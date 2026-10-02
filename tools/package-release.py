"""Package already verified native outputs from a committed source checkpoint.

No build, signing, or publishing occurs here. Keys, caches and family records are
never read. Run after the focused QA ledger is complete and Git source is clean.
"""
import argparse
import hashlib
import io
import json
import pathlib
import re
import shutil
import subprocess
import tarfile
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('version')
parser.add_argument('--output', type=pathlib.Path, required=True)
args = parser.parse_args()
assert re.fullmatch(r'\d+\.\d+\.\d+', args.version)
repo = pathlib.Path(__file__).resolve().parent.parent
def git(*command):
    return subprocess.check_output(['git', '-C', str(repo), *command])
assert not git('status', '--porcelain').strip(), 'Commit source/QA changes before packaging.'
commit = git('rev-parse', 'HEAD').decode().strip()
build = git('show', 'HEAD:android/app/build.gradle.kts').decode()
assert f'versionName = "{args.version}"' in build
qa = json.loads((repo / 'android/docs/evidence/continuation-2026-10-02/final-device-summary.json').read_text())
assert qa['passed'] >= 21 and qa['failed'] == 0 and not qa['incomplete_logs']
output = args.output.resolve()
output.mkdir(parents=True, exist_ok=True)
apk = output / f'SproutBook-{args.version}-debug.apk'
shutil.copyfile(repo / 'android/app/build/outputs/apk/debug/app-debug.apk', apk)
source = output / f'SproutBook-{args.version}-source.zip'
prefix = f'SproutBook-{args.version}/'
manifest = {'version': args.version, 'source_commit': commit,
            'release_outputs': 'Unsigned R8 APK/AAB; evaluation APK is a separate asset.',
            'evaluation_apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'files': {}}
def write(archive, name, data, executable=False):
    assert not name.lower().endswith(('.keystore', '.jks', '.p12', '.pem')) and '/.env' not in name
    info = zipfile.ZipInfo(prefix + name, (2026, 10, 2, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    info.create_system = 3
    info.external_attr = (0o100755 if executable else 0o100644) << 16
    archive.writestr(info, data)
    manifest['files'][name] = {'size': len(data), 'sha256': hashlib.sha256(data).hexdigest()}
paths = ['android', 'website', 'tools', 'docs/CONTINUATION.md', 'docs/SOURCE-ARCHIVE.md', 'docs/evidence']
tar = git('archive', '--format=tar', 'HEAD', *paths)
with zipfile.ZipFile(source, 'w') as archive:
    with tarfile.open(fileobj=io.BytesIO(tar)) as committed:
        for entry in committed:
            if not entry.isfile():
                assert entry.isdir(), f'Unexpected non-regular source entry: {entry.name}'
                continue
            data = committed.extractfile(entry).read()
            write(archive, entry.name, data, bool(entry.mode & 0o111))
            if entry.name == 'docs/SOURCE-ARCHIVE.md':
                write(archive, 'README.md', data)
    for name, path in [
        ('SproutBook-' + args.version + '-release-unsigned.apk', 'app/build/outputs/apk/release/app-release-unsigned.apk'),
        ('SproutBook-' + args.version + '-release.aab', 'app/build/outputs/bundle/release/app-release.aab'),
        ('mapping.txt', 'app/build/outputs/mapping/release/mapping.txt'),
    ]:
        write(archive, 'android/release-artifacts/' + name, (repo / 'android' / path).read_bytes())
    write(archive, 'SOURCE-MANIFEST.json', json.dumps(manifest, sort_keys=True, indent=2).encode() + b'\n')
with zipfile.ZipFile(source) as archive:
    assert archive.testzip() is None
    assert prefix + 'android/gradlew' in archive.namelist()
    assert archive.read(prefix + 'android/app/build.gradle.kts').decode() == build
    assert any('/android/app/src/main/' in name and name.endswith('.kt') for name in archive.namelist())
sums = ''.join(f'{hashlib.sha256(file.read_bytes()).hexdigest()}  {file.name}\n' for file in [apk, source])
checksum = output / f'SHA256SUMS-{args.version}.txt'
checksum.write_text(sums)
print(json.dumps({'source_commit': commit, 'apk_bytes': apk.stat().st_size,
                  'source_bytes': source.stat().st_size, 'checksums': sums}, indent=2))
