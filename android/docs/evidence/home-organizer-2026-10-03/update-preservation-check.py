import hashlib,json,pathlib,sqlite3,subprocess,sys
stage=sys.argv[1]
root=pathlib.Path('/workspace/Sproutbook/artifacts/update-3.7.0')
dest=root/stage;dest.mkdir(parents=True,exist_ok=True)
adb='/workspace/scratch/sprout-toolchain/android-sdk/platform-tools/adb';pkg='com.nothatcher.sproutbook'
def run(*args):return subprocess.run([adb,'-s','emulator-5554',*args],check=True,capture_output=True,timeout=60).stdout
run('shell','am','force-stop',pkg)
for name in ['sproutbook-v3.db','sproutbook-v3.db-wal','sproutbook-v3.db-shm']:
 r=subprocess.run([adb,'-s','emulator-5554','exec-out','run-as',pkg,'cat','databases/'+name],capture_output=True,timeout=60)
 if r.returncode==0:(dest/name).write_bytes(r.stdout)
 elif name=='sproutbook-v3.db':raise RuntimeError(r.stderr.decode())
prefs=run('exec-out','run-as',pkg,'cat','files/datastore/family_settings.preferences_pb');(dest/'settings.pb').write_bytes(prefs)
c=sqlite3.connect(dest/'sproutbook-v3.db');c.row_factory=sqlite3.Row
assert c.execute('pragma integrity_check').fetchone()[0]=='ok'
tables=sorted(r[0] for r in c.execute("select name from sqlite_master where type='table'") if not r[0].startswith('sqlite_') and r[0] not in ['room_master_table','android_metadata','room_table_modification_log'])
rows={t:sorted([dict(r) for r in c.execute('select * from "'+t+'"')],key=lambda r:r.get('id','')) for t in tables}
version=c.execute('pragma user_version').fetchone()[0];c.close();(root/(stage+'.json')).write_text(json.dumps(rows,sort_keys=True,indent=2))
summary={'schema':version,'counts':{k:len(v) for k,v in rows.items()},'settings_sha256':hashlib.sha256(prefs).hexdigest(),'integrity':'ok'}
if stage=='before':assert version==12 and len(rows['children'])==2 and len(rows['wishlistItems'])==1
else:
 before=json.loads((root/'before.json').read_text());assert version==13
 for table,old in before.items():
  cols=old[0].keys() if old else [];new=rows[table]
  assert len(old)==len(new),(table,len(old),len(new))
  assert old==[{k:r[k] for k in cols} for r in new],table
 assert all(r['homeSections']=='' and r['homeHiddenSections']=='' and r['homeQuickActions']=='memory,schedule' and r['homeBackground']=='woodland' and r['homeBackgroundPhoto'] is None for r in rows['children'])
 assert prefs==(root/'before/settings.pb').read_bytes()
 summary.update({'every_existing_column_and_row_preserved':True,'new_home_defaults':'original order/no hidden sections/memory,schedule/woodland/no photo','datastore_bytes_preserved':True,'offline_native_startup':'Wi-Fi/data disabled; actual native startup recorded separately'})
 out=pathlib.Path('/workspace/Sproutbook/android/docs/evidence/home-organizer-2026-10-03/update-preservation.json');out.write_text(json.dumps(summary,sort_keys=True,indent=2)+'\n')
print(json.dumps(summary,sort_keys=True,indent=2))
