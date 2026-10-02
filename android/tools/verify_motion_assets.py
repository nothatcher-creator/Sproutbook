"""Validate packaged motion assets against the original manifest and archive."""
import argparse, json, pathlib, struct, zipfile

def verify(root, archive=None):
    manifest = json.loads((root/'docs/motion-pack/manifest.json').read_text())
    assert manifest['asset_count'] == len(manifest['assets']) == 46
    ids = {a['id'] for a in manifest['assets']}
    assert len(ids) == 46
    z = zipfile.ZipFile(archive) if archive else None
    for theme in manifest['variants']:
        for a in manifest['assets']:
            for kind, ext in [('lottie','json'), ('png','png')]:
                rel = f'{kind}/{theme}/{a["id"]}.{ext}'
                data = (root/'app/src/main/assets/sproutbook-motion'/rel).read_bytes()
                if z:
                    assert data == z.read('SproutBook-Woodland-Motion-Pack/'+rel), rel
                if kind == 'lottie':
                    content=json.loads(data)
                    assert (content['w'],content['h']) == (a['w'],a['h']), rel
                    assert content['layers'] and content['fr'] == 24 and content['op'] > content['ip'], rel
                    assert abs((content['op']-content['ip'])/content['fr']-a['duration']) < .05, rel
                    assert not content.get('assets'), 'External images prohibited: '+rel
                else:
                    assert data[:8] == b'\x89PNG\r\n\x1a\n', rel
                    assert all(v>0 for v in struct.unpack('>II', data[16:24])), rel
    if z: z.close()
    print('PASS: 92 native animations + 92 PNG fallbacks; manifest valid; originals unchanged' if archive else 'PASS: complete asset contract')

if __name__ == '__main__':
    p=argparse.ArgumentParser();p.add_argument('--archive');args=p.parse_args()
    verify(pathlib.Path(__file__).resolve().parents[1],args.archive)
