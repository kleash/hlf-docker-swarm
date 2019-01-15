package com.github.kleash.fabric.sdk.java.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

public class Test {

	final HFClient client = HFClient.createNewInstance();
	Channel channel;
	QueryByChaincodeRequest qpr;

	String clientFabricPrivateLocation = "/home/shubham/MDrive/WORK/fabric/hlf-certs/certs/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore";
	String clientFabricCertLocation = "/home/shubham/MDrive/WORK/fabric/hlf-certs/certs/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem";
	static String peerFabricTLSCertLocation = "/home/shubham/MDrive/WORK/fabric/hlf-certs/certs/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt";
	static String ordererFabricTLSCertLocation = "/home/shubham/MDrive/WORK/fabric/hlf-certs/certs/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/tls/server.crt";

	void setupCryptoMaterialsForClient() throws Exception {
		// Set default crypto suite for HF client

		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

		client.setUserContext(new User() {

			public String getName() {
				return "PeerAdmin";
			}

			public Set<String> getRoles() {
				return null;
			}

			public String getAccount() {
				return null;
			}

			public String getAffiliation() {
				return null;
			}

			public Enrollment getEnrollment() {
				return new Enrollment() {
					public PrivateKey getKey() {
						PrivateKey privateKey = null;
						try {
							File privateKeyFile = findFileSk(clientFabricPrivateLocation);
							privateKey = getPrivateKeyFromBytes(
									IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
						} catch (InvalidKeySpecException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (NoSuchProviderException e) {
							e.printStackTrace();
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						return privateKey;
					}

					public String getCert() {

						String certificate = null;
						try {
							File certificateFile = new File(clientFabricCertLocation);
							certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)),
									"UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return certificate;
					}
				};
			}

			public String getMspId() {
				return "Org1MSP";
			}
		});
	}

	static Channel getChannel(HFClient client) throws InvalidArgumentException, TransactionException {
		// initialize channel
		Properties peerProperties = new Properties();
		peerProperties.setProperty("pemFile", peerFabricTLSCertLocation);
		peerProperties.setProperty("trustServerCertificate", "true"); // testing // // PRODUCTION!
		peerProperties.setProperty("hostnameOverride", "peer0.org1.example.com");
		peerProperties.setProperty("sslProvider", "openSSL");
		peerProperties.setProperty("negotiationType", "TLS");
		peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

		// eventhub name and endpoint in fabcar network
//		EventHub eventHub = client.newEventHub("eventhub01", "grpcs://localhost:7053");

		// orderer properties
		Properties ordererProperties = new Properties();
		ordererProperties.setProperty("pemFile", ordererFabricTLSCertLocation);
		ordererProperties.setProperty("trustServerCertificate", "true"); // testing
																			// environment
																			// only
																			// NOT
																			// FOR
																			// PRODUCTION!
		ordererProperties.setProperty("hostnameOverride", "orderer.example.com");
		ordererProperties.setProperty("sslProvider", "openSSL");
		ordererProperties.setProperty("negotiationType", "TLS");
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });

		// channel name in fabcar network
		Channel channel = client.newChannel("mychannel");

		channel.addPeer(client.newPeer("peer0.org1.example.com", "grpcs://3.17.155.192:7051", peerProperties)); // use
																												// the
																												// network
																												// peer
																												// container
																												// URL
//		channel.addEventHub(eventHub);
		channel.addOrderer(client.newOrderer("orderer.example.com", "grpcs://3.17.155.192:7050", ordererProperties)); // use
																														// the
																														// network
																														// orderer
																														// container
																														// URL
		channel.initialize();

		return channel;
	}

	static File findFileSk(String directorys) {

		File directory = new File(directorys);

		File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

		if (null == matches) {
			throw new RuntimeException(
					"Matches returned null does " + directory.getAbsoluteFile().getName() + " directory exist?");
		}

		if (matches.length != 1) {
			throw new RuntimeException("Expected in " + directory.getAbsoluteFile().getName()
					+ " only 1 sk file but found " + directory.getAbsoluteFile().getName() + matches.length);
		}

		return matches[0];
	}

	static PrivateKey getPrivateKeyFromBytes(byte[] data)
			throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
		final Reader pemReader = new StringReader(new String(data));

		final PrivateKeyInfo pemPair;
		try (PEMParser pemParser = new PEMParser(pemReader)) {
			pemPair = (PrivateKeyInfo) pemParser.readObject();
		}

		PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.getPrivateKey(pemPair);

		return privateKey;
	}

//	void createChannel() throws InvalidArgumentException, TransactionException, ProposalException {
//		channel = client.newChannel("mychannel");
//		Properties ordererProperties = new Properties();
//		ordererProperties.setProperty("pemFile",
//				"D:/FabricCert/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt");
//		ordererProperties.setProperty("trustServerCertificate", "true"); // testing
//																			// environment
//																			// only
//																			// NOT
//																			// FOR
//																			// PRODUCTION!
//		ordererProperties.setProperty("hostnameOverride", "orderer.example.com");
//		ordererProperties.setProperty("sslProvider", "openSSL");
//		ordererProperties.setProperty("negotiationType", "TLS");
//		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
//		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });
//		channel.addOrderer(client.newOrderer("orderer.example.com", "grpcs://localhost:7050", ordererProperties)); // use
//																													// the
//																													// network
//																													// orderer
//																													// container
//																													// URL
//
//		Properties peerProperties = new Properties();
//		peerProperties.setProperty("pemFile",
//				"D:/FabricCert/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt");
//		peerProperties.setProperty("trustServerCertificate", "true"); // testing // // PRODUCTION!
//		peerProperties.setProperty("hostnameOverride", "peer0.org1.example.com");
//		peerProperties.setProperty("sslProvider", "openSSL");
//		peerProperties.setProperty("negotiationType", "TLS");
//		peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
//
//		channel.addPeer(client.newPeer("peer0.org1.example.com", "grpcs://localhost:7051", peerProperties)); // use the
//																												// network
//																												// peer
//																												// container
//																												// URL
//
//		channel.initialize();
//	}

	void queryChain()
			throws InvalidArgumentException, ProposalException, ChaincodeEndorsementPolicyParseException, IOException {

		// get channel instance from client

		Channel channel2 = client.getChannel("mychannel");

		int blocksize = (int) channel2.queryBlockchainInfo().getHeight();
		System.out.println("NO of Blocks: " + blocksize);

		// create chaincode request
		qpr = client.newQueryProposalRequest();
		// build cc id providing the chaincode name. Version is omitted here.
		ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("simple").build();
		qpr.setChaincodeID(fabcarCCId);
		// CC function to be called.
		qpr.setFcn("query");
		qpr.setArgs(new String[] { "account1" });

		Collection<ProposalResponse> res = channel2.queryByChaincode(qpr, channel2.getPeers());

		// display response
		for (ProposalResponse pres : res) {

			String stringResponse = new String(pres.getChaincodeActionResponsePayload());
			System.out.println("Query Response from Peer " + pres.getPeer().getName() + ":" + stringResponse);

		}
	}

	void invokeChain()
			throws InvalidArgumentException, ProposalException, ChaincodeEndorsementPolicyParseException, IOException {

		Channel channel = client.getChannel("mychannel");
		TransactionProposalRequest req = client.newTransactionProposalRequest();
		ChaincodeID cid = ChaincodeID.newBuilder().setName("simple").build();
		req.setChaincodeID(cid);
		req.setFcn("invoke");
		req.setArgs(new String[] { "b", "a", "5" });
		Collection<ProposalResponse> resps = channel.sendTransactionProposal(req);

		channel.sendTransaction(resps);
		System.out.println(resps.iterator().next().getMessage());

	}

	public static void main(String args[]) throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Test t = new Test();
		t.setupCryptoMaterialsForClient();

		// set channel
		Channel channel = getChannel(t.client);

//		t.createChannel();
//		t.invokeChain();
		t.queryChain(); // For querying

	}
}
