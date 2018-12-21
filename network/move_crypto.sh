# sudo mkdir -p /var/mynetwork
# sudo chown -R $(whoami) /var/mynetwork
# sudo chown -R $USER:$USER /var/mynetwork
rm -rf mkdir /home/ubuntu/efsmount/mynetwork/*

mkdir -p /home/ubuntu/efsmount/mynetwork/chaincode
mkdir -p /home/ubuntu/efsmount/mynetwork/certs
mkdir -p /home/ubuntu/efsmount/mynetwork/bin

# git clone https://github.com/hyperledger/fabric /var/mynetwork/fabric-src/hyperledger/fabric
#cd /var/mynetwork/fabric-src/hyperledger/fabric
#git checkout release-1.1
cd -
cp -R crypto-config /home/ubuntu/efsmount/mynetwork/certs/
cp -R config /home/ubuntu/efsmount/mynetwork/certs/
cp -R ../chaincodes/* /home/ubuntu/efsmount/mynetwork/chaincode/
cp -R bin/* /home/ubuntu/efsmount/mynetwork/bin/