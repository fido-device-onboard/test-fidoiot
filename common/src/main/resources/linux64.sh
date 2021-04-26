#!/bin/bash
wget https://raw.githubusercontent.com/secure-device-onboard/pri-fidoiot/master/SECURITY.md
filename=SECURITY.md
cksum_tx=2749598590
cksum_rx=$(cksum $filename | cut -d ' ' -f 1)
if [ $cksum_tx -eq $cksum_rx  ]; then
  echo "Device onboarded successfully."
  echo "Device onboarded successfully." > result.txt
else
  echo "ServiceInfo file transmission failed."
  echo "ServiceInfo file transmission failed." > result.txt
fi