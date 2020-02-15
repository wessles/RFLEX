package com.wessles.crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.badlogic.gdx.utils.Base64Coder;

public class KeyUtil {

	// Creates a public key object provided with a Base64 encoded public key
	public static PublicKey getPublicKey(String encoded) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(new X509EncodedKeySpec(Base64Coder.decode(encoded)));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PrivateKey getPrivateKey(String encoded) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64Coder.decode(encoded)));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void printKeyPair() {
		KeyPairGenerator gen;
		try {
			gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			KeyPair pair = gen.generateKeyPair();
			System.out.println("Public: " + new String(Base64Coder.encode(pair.getPublic().getEncoded())));
			System.out.println("Private: " + new String(Base64Coder.encode(pair.getPrivate().getEncoded())));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
