# sunjce-vs-bc-performance
Compare performance of SunJCE Cipher for Peppol encryption algorithm vs Bouncy Castle

Algorithm URI: http://www.w3.org/2009/xmlenc11#aes128-gcm

Algorithm code: AES/GCM/NoPadding

## Steps

1. Generate test file of given size with random binary contents, calculate its digest.
1. Install or remove BC provider
1. Init WSSConfig
1. Create key like it is done in
   https://github.com/apache/cxf/blob/dc4477f563acddc522018d9ef817304096b57a70/rt/ws/security/src/main/java/org/apache/cxf/ws/security/wss4j/policyhandlers/AsymmetricBindingHandler.java#L243-L245
1. Create Cipher like it is done in
   https://github.com/apache/ws-wss4j/blob/d7c26fcfc40f4a357ae0d6bf06308014167b5bca/ws-security-dom/src/main/java/org/apache/wss4j/dom/message/Encryptor.java#L419
1. Read file without any action except counting bytes
1. Encrypt file to file system like it is done in
   https://github.com/apache/ws-wss4j/blob/d7c26fcfc40f4a357ae0d6bf06308014167b5bca/ws-security-common/src/main/java/org/apache/wss4j/common/util/AttachmentUtils.java#L562
1. Decrypt result to file system like it is done in
   https://github.com/apache/ws-wss4j/blob/d7c26fcfc40f4a357ae0d6bf06308014167b5bca/ws-security-common/src/main/java/org/apache/wss4j/common/util/AttachmentUtils.java#LL486C26-L486C26
1. Calculate decrypted file digest and compare with original.

## Some conclusions 

1. **On encryption, SunJCE performs better then BC** - less than 300 ms on 100 MB vs. more than 1,5 second by BC
1. **SunJCE decryption performance significantly degrades from 5 MB payload and more**. The worst performance is on Java 12 (even Java 8 performed better), but later versions are faster, although even Java 20 needs 11 minutes to decrypt 100 MB payload, when BC does it in less than 2 seconds for this (380 times faster!).
1. For up to 5 MB of gzipped payload (Oxalis always gzips it, but other implementations can skip it) - the performance of decryption is relatively acceptable, <=3 seconds
1. Starting from 10 MB SunJCE decryption exceeds 5 seconds, and *for 100 MB payload can take 11 mins (Java 20), 12 mins (Java 17), 38 mins (Java 12), 22 mins (Java 8)*.

## Result tables

All time in ms.

#### Java 1.8.0_372: OpenJDK 64-Bit Server VM 25.372-b07
#### SunJCE version 1.8 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 16 | 10 | 1,6 | 13 | 6 | 2,2 |
| 0,5 MB | 11 | 16 | 0,7 | 56 | 16 | 3,5 |
| 1,0 MB | 21 | 17 | 1,2 | 182 | 20 | 9,1 |
| 5,0 MB | 25 | 67 | 0,4 | 2.193 | 82 | 26,7 |
| 10,0 MB | 42 | 128 | 0,3 | 8.826 | 151 | 58,5 |
| 100,0 MB | 419 | 1.252 | 0,3 | 1.329.506 | 1.546 | 860,0 |

#### Java 12: OpenJDK 64-Bit Server VM 12+33
#### SunJCE version 12 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 16 | 11 | 1,5 | 36 | 7 | 5,1 |
| 0,5 MB | 16 | 10 | 1,6 | 86 | 13 | 6,6 |
| 1,0 MB | 34 | 23 | 1,5 | 298 | 28 | 10,6 |
| 5,0 MB | 16 | 94 | 0,2 | 3.280 | 99 | 33,1 |
| 10,0 MB | 26 | 169 | 0,2 | 15.209 | 173 | 87,9 |
| 100,0 MB | 225 | 1.530 | 0,1 | 2.261.457 | 1.665 | 1358,2 |

#### Java 17: OpenJDK 64-Bit Server VM 17+35-2724
#### SunJCE version 17 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 27 | 15 | 1,8 | 23 | 7 | 3,3 |
| 0,5 MB | 21 | 15 | 1,4 | 89 | 20 | 4,4 |
| 1,0 MB | 37 | 26 | 1,4 | 229 | 30 | 7,6 |
| 5,0 MB | 51 | 127 | 0,4 | 2.081 | 129 | 16,1 |
| 10,0 MB | 30 | 178 | 0,2 | 7.686 | 204 | 37,7 |
| 100,0 MB | 262 | 1.657 | 0,2 | 744.611 | 1.905 | 390,9 |

#### Java 20.0.1: OpenJDK 64-Bit Server VM 20.0.1+9
#### SunJCE version 20 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 26 | 17 | 1,5 | 25 | 10 | 2,5 |
| 0,5 MB | 19 | 14 | 1,4 | 91 | 15 | 6,1 |
| 1,0 MB | 32 | 26 | 1,2 | 235 | 35 | 6,7 |
| 5,0 MB | 40 | 115 | 0,3 | 2.025 | 115 | 17,6 |
| 10,0 MB | 27 | 177 | 0,2 | 7.469 | 195 | 38,3 |
| 100,0 MB | 270 | 1.608 | 0,2 | 705.306 | 1.849 | 381,5 |
