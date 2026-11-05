// Prints emulator-out/results.txt in compact form. Wrapped so that nothing here can ever fail the job.
const fs = require('fs');
try {
  const f = 'emulator-out/results.txt';
  if (fs.existsSync(f)) {
    const lines = fs.readFileSync(f, 'utf8').split('\n');
    const n = (p) => lines.filter((l) => l.startsWith(p)).length;
    console.log('===== RESULTS: ' + n('PASS') + ' PASS, ' + n('FAIL') + ' FAIL, ' + n('INFO') + ' INFO =====');
    lines.filter((l) => l.startsWith('FAIL')).forEach((l) => console.log(l.slice(0, 300)));
    lines.filter((l) => l.startsWith('INFO')).forEach((l) => console.log(l.slice(0, 380)));
    console.log('===== END RESULTS =====');
  } else {
    console.log('===== NO results.txt (the scenario did not get that far) =====');
  }
} catch (e) {
  console.log('results post-step error (ignored): ' + e);
}
process.exit(0);
