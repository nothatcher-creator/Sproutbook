"""Reproducible local tool bootstrap for ephemeral Linux QA hosts (not needed in Android Studio)."""
import os,pathlib,urllib.request,zipfile,tarfile,hashlib,xml.etree.ElementTree as ET,concurrent.futures,shutil
root=pathlib.Path('/tmp/sprout-toolchain');root.mkdir(exist_ok=True)
archive_cache=pathlib.Path(os.environ.get('SPROUT_ARCHIVE_CACHE',str(root)));archive_cache.mkdir(parents=True,exist_ok=True)
def download(url,name,sha1=None):
 p=root/name
 cached=archive_cache/name
 if cached!=p and cached.exists():shutil.copy2(cached,p)
 if not p.exists():
  print('Downloading',name,flush=True)
  with urllib.request.urlopen(url,timeout=90) as response,p.open('wb') as out:shutil.copyfileobj(response,out)
 if sha1 and hashlib.sha1(p.read_bytes()).hexdigest()!=sha1:raise RuntimeError('Archive checksum mismatch: '+name)
 if cached!=p:shutil.copy2(p,cached)
 return p
def tool(kind):
 if kind=='java':
  if pathlib.Path('/usr/lib/jvm/java-17-openjdk-amd64/bin/javac').exists():return
  p=download('https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.tar.gz','jdk17.tar.gz')
  with tarfile.open(p) as t:t.extractall(root,filter='data')
  return
 if kind=='gradle':
  p=download('https://downloads.gradle.org/distributions/gradle-9.3.1-bin.zip','gradle-9.3.1.zip')
  expected=urllib.request.urlopen('https://downloads.gradle.org/distributions/gradle-9.3.1-bin.zip.sha256').read().decode().strip()
  if hashlib.sha256(p.read_bytes()).hexdigest()!=expected:raise RuntimeError('Gradle checksum mismatch')
  with zipfile.ZipFile(p) as z:z.extractall(root)
  return
 xml=ET.fromstring(urllib.request.urlopen('https://dl.google.com/android/repository/repository2-3.xml').read())
 sdk=root/'android-sdk';sdk.mkdir(exist_ok=True)
 paths=['platforms;android-37.0','build-tools;36.0.0','platform-tools','emulator']
 for target in paths:
  pkg=next(x for x in xml.findall('remotePackage') if x.attrib['path']==target)
  archives=pkg.find('archives').findall('archive');archive=next(x for x in archives if x.findtext('host-os') in (None,'linux'))
  complete=archive.find('complete');url=complete.findtext('url');name=url.rsplit('/',1)[-1]
  p=download('https://dl.google.com/android/repository/'+url,name,complete.findtext('checksum'))
  destination=sdk/pathlib.Path(*target.split(';'));temp=root/('extract-'+target.replace(';','-'));temp.mkdir(exist_ok=True)
  with zipfile.ZipFile(p) as z:z.extractall(temp)
  children=list(temp.iterdir());src=children[0] if len(children)==1 and children[0].is_dir() else temp
  destination.parent.mkdir(parents=True,exist_ok=True);shutil.copytree(src,destination,dirs_exist_ok=True)
  for f in destination.rglob('*'):
   if f.is_file() and (f.suffix in ('','.sh') or f.name in ('aapt','aapt2','adb','emulator','zipalign','apksigner','d8')):f.chmod(f.stat().st_mode|0o111)
  print('Installed',target,flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:list(pool.map(tool,['java','gradle','sdk']))
for p in (root/'gradle-9.3.1/bin').iterdir():p.chmod(0o755)
(sdk:=root/'android-sdk'/'licenses').mkdir(exist_ok=True)
(sdk/'android-sdk-license').write_text('8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e\n24333f8a63b6825ea9c5514f83c2829b004d1fee\n')
print('Toolchain ready',flush=True)
