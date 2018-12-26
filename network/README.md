## hlf-multichannel-docker-swarm

- Create the swarm and network
```
./setup_swarm.sh
./create_network.sh
```

- After Joining, move crypto certs to common folders
```
./move_crypto.sh
```
- Set hostname of all 3 machines in .env
```
nano .env
```
- Populate hostname in all compose yamls
```
./populate_hostname.sh
```
- Deploy the containers
```
./start_all.sh
```
- Check services status
```
docker service ls | grep "0/1"
```
- Setup channels and chaincode
```
./scripts/create_channel.sh
./scripts/install_chaincodes.sh
```
