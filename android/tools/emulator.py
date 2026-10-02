import os,pathlib,urllib.request,xml.etree.ElementTree as ET,zipfile,hashlib,shutil
root=pathlib.Path(os.environ.get('SPROUT_TOOLCHAIN_ROOT','/tmp/sprout-toolchain'));sdk=root/'android-sdk'
xml=ET.fromstring(urllib.request.urlopen('https://dl.google.com/android/repository/sys-img/android/sys-img2-1.xml').read())
pkg=next(p for p in xml.findall('remotePackage') if p.attrib['path']=='system-images;android-29;default;x86_64')
a=pkg.find('archives/archive/complete');url=a.findtext('url');p=root/url.rsplit('/',1)[-1]
print('Downloading system image',flush=True)
if not p.exists():
 with urllib.request.urlopen('https://dl.google.com/android/repository/sys-img/android/'+url,timeout=90) as r,p.open('wb') as out:shutil.copyfileobj(r,out)
assert hashlib.sha1(p.read_bytes()).hexdigest()==a.findtext('checksum')
dest=sdk/'system-images/android-29/default';dest.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(p) as z:z.extractall(dest)
avd=pathlib.Path(os.environ.get('ANDROID_AVD_HOME',str(pathlib.Path.home()/'.android/avd')));avd.mkdir(parents=True,exist_ok=True);config=avd/'sproutbook-qa.avd';config.mkdir(exist_ok=True)
(avd/'sproutbook-qa.ini').write_text('avd.ini.encoding=UTF-8\npath='+str(config)+'\ntarget=android-29\n')
(config/'config.ini').write_text('''avd.ini.encoding=UTF-8
abi.type=x86_64
hw.cpu.arch=x86_64
hw.cpu.ncore=1
hw.ramSize=1536
hw.lcd.width=360
hw.lcd.height=800
hw.lcd.density=160
hw.gpu.enabled=yes
hw.gpu.mode=swiftshader
hw.audioInput=no
hw.camera.back=none
hw.camera.front=none
hw.keyboard=yes
hw.mainKeys=no
hw.dPad=no
hw.trackBall=no
image.sysdir.1='''+str(dest/'x86_64')+'''/
disk.dataPartition.size=2048M
tag.id=default
tag.display=Default
PlayStore.enabled=false
''')
print('AVD ready',flush=True)
