package com.mercell.rd.jca;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.cxf.helpers.IOUtils;
import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.util.AttachmentUtils;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.XMLCipherUtil;
import org.apache.xml.security.stax.ext.XMLSecurityConstants;
import org.apache.xml.security.utils.EncryptionConstants;

import com.google.common.io.CountingOutputStream;

public class Main {

	private static final boolean DO_READ_FILE_AT_FIRST = true;

	private static final boolean VERBOSE = false;

	public static void main(String[] args) throws Exception {
		int maxMBSize = -1;
		if (args.length > 0) {
			String maxMBSizeStr = args[0];
			try {
				maxMBSize = Integer.parseInt(maxMBSizeStr);
			} catch (Exception e) {
				System.err.println("Argument should be integer meaning maximum size of test file to test in MB, e.g. 10 or 5");
				System.exit(2);
				return;
			}
		}
		Main c = new Main();

		float sizeMBList[] = new float[] { 0.1f, 0.5f, 1f, 5f, 10f, 100f };

		String javaVersion = "Java " + System.getProperty("java.version") + ": " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");

		List<FileSizeResult> fileSizeResultList = new ArrayList<FileSizeResult>();

		System.out.println("PROGRESS: " + javaVersion);

		for (float sizeMB : sizeMBList) {
			if (maxMBSize > 0 && sizeMB > maxMBSize) {
				break;
			}
			FileSizeResult fileSizeResult = new FileSizeResult();
			fileSizeResult.sizeMB = sizeMB;

			System.out.println("PROGRESS: Test random file encryption/decryption of size " + sz(sizeMB));

			File testFile = c.generateTestFile(sizeMB);
			try {
				String digest = calcualteDigest(testFile);
				fileSizeResult.bc = c.run(testFile, digest, true);
				fileSizeResult.sun = c.run(testFile, digest, false);

				fileSizeResultList.add(fileSizeResult);
			} finally {
				testFile.delete();
			}
		}

		String header1 = String.format("#### %s", javaVersion);
		FileSizeResult fr = fileSizeResultList.get(0);
		String header2 = String.format("#### %s vs. %s", fr.sun.provider, fr.bc.provider);
		String header3 = formatMDRow("Size", "Encrypt Sun", "Encrypt BC", "Encrypt Sun/BC", "Decrypt Sun", "Decrypt BC", "Decrypt Sun/BC");

		System.out.println(header1);
		System.out.println(header2);
		System.out.println(header3);
		System.out.println(formatMDRow(Collections.nCopies(7, "---").toArray(new String[] {})));
		for (FileSizeResult r : fileSizeResultList) {
			String row = formatMDRow(sz(r.sizeMB), ms(r.sun.encrypt), ms(r.bc.encrypt), ratio((float) r.sun.encrypt / r.bc.encrypt), ms(r.sun.decrypt), ms(r.bc.decrypt), ratio((float) r.sun.decrypt / r.bc.decrypt));
			System.out.println(row);
		}
	}

	private static String formatMDRow(String... sl) {
		return "| " + String.join(" | ", sl) + " |";
	}

	private static String ratio(float ratio) {
		return String.format("%.1f", ratio);
	}

	private static String sz(float sizeMB) {
		return String.format("%03.1f", sizeMB) + " MB";
	}

	private static String ms(long d) {
		return String.format("%,d", d).replace(',', '.');
	}

	private static final class FileSizeResult {
		float sizeMB;
		RunResult bc;
		RunResult sun;
	}

	public File generateTestFile(float fileSizeMb) throws Exception {
		File file = File.createTempFile("JCAPerformanceComp_", "_" + sz(fileSizeMb).replace(' ', '_').replace(',', '.') + ".dat");

		Random r = new Random();

		byte[] buffer = new byte[1024 * 10];

		long start = System.currentTimeMillis();

		int count = 0;
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			while (count < fileSizeMb * 1024 * 1024) {
				r.nextBytes(buffer);
				out.write(buffer);
				count += buffer.length;
			}
		}

		if (VERBOSE) {
			System.out.println("Generated file " + file + " in " + (System.currentTimeMillis() - start));
		}
		return file;
	}

	private static String calcualteDigest(File file) throws Exception {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, sha256)) {
				byte[] buffer = new byte[1024 * 10];
				while (digestInputStream.read(buffer) != -1) {
				}
			}
		}
		byte[] checksum = sha256.digest();
		return org.bouncycastle.util.encoders.Base64.toBase64String(checksum);
	}

	private static class RunResult {
		String provider;
		long encrypt;
		long decrypt;
	}

	public RunResult run(File testFile, String digest, boolean useBC) throws Exception {
		if (useBC) {
			Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
		} else {
			Security.removeProvider("BC");
		}

		Timer timer = new Timer();

		timer.start("Create key");
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey secretKey = keyGen.generateKey();

		timer.stopStart("Init WSSConfig");
		WSSConfig.init();

		timer.stopStart("Create Cipher");
		String algorithm = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM;
		Cipher cipher = createCipher(algorithm, secretKey);

		if (DO_READ_FILE_AT_FIRST) {
			timer.start("Read file only");
			try (InputStream sourceStream = new BufferedInputStream(new FileInputStream(testFile)); CountingOutputStream out = new CountingOutputStream(new NullOutputStream())) {
				IOUtils.copy(sourceStream, out);
				timer.stopStart("File size: " + out.getCount());
			}
		}

		timer.stopStart("Encrypt with " + cipher.getProvider());

		File encryptedFilePath = new File(testFile.getAbsolutePath() + ".enc");
		File decryptedFilePath = new File(testFile.getAbsolutePath() + ".enc.dec");

		RunResult runResult = new RunResult();
		runResult.provider = cipher.getProvider().toString();
		try {
			try (InputStream sourceStream = new BufferedInputStream(new FileInputStream(testFile))) {
				Attachment a = new Attachment();
				a.setSourceStream(sourceStream);
				InputStream encryptionStream = AttachmentUtils.setupAttachmentEncryptionStream(cipher, false, a, null);
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(encryptedFilePath))) {
					IOUtils.copy(encryptionStream, out);
				}
			}

			runResult.encrypt = timer.stopStart("Decrypt with " + cipher.getProvider());
			try (InputStream sourceStream = new BufferedInputStream(new FileInputStream(encryptedFilePath))) {
				InputStream decryptionStream = AttachmentUtils.setupAttachmentDecryptionStream(algorithm, cipher, secretKey, sourceStream);
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(decryptedFilePath))) {
					IOUtils.copy(decryptionStream, out);
				}
			}

			runResult.decrypt = timer.stopStart("Calculate digest");
			String decryptedDigest = calcualteDigest(decryptedFilePath);

			if (!digest.equals(decryptedDigest)) {
				System.out.println("UNEXEPCTED RESULT - SHA-256 digest of original file does not equal to decrypted");
				System.exit(1);
				return null;
			}

		} finally {
			encryptedFilePath.delete();
			decryptedFilePath.delete();
		}

		if (VERBOSE) {
			System.out.println("Total\t" + timer.finish());
			System.out.println(timer.toReport());
		}

		return runResult;
	}

	/*
	 * Copied from org.apache.wss4j.dom.message.Encryptor.createCipher(String, SecretKey)
	 */
	private static Cipher createCipher(String encryptionAlgorithm, SecretKey secretKey) throws WSSecurityException {
		String jceAlgorithm = JCEMapper.translateURItoJCEID(encryptionAlgorithm);
		try {
			Cipher cipher = Cipher.getInstance(jceAlgorithm);

			int ivLen = JCEMapper.getIVLengthFromURI(encryptionAlgorithm) / 8;
			byte[] iv = XMLSecurityConstants.generateBytes(ivLen);
			AlgorithmParameterSpec paramSpec = XMLCipherUtil.constructBlockCipherParameters(encryptionAlgorithm, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);

			return cipher;
		} catch (Exception e) {
			throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_ENCRYPTION, e);
		}
	}

	private static class NullOutputStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException {
		}

		@Override
		public void write(byte[] b) throws IOException {
		}

	}
}
