# sunjce-vs-bc-performance
Compare performance of SunJCE Cipher for Peppol encryption algorithm vs Bouncy Castle

Algorithm URI: http://www.w3.org/2009/xmlenc11#aes256-gcm

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

## Results

All time in ms.

#### Java 1.8.0_372: OpenJDK 64-Bit Server VM 25.372-b07
#### SunJCE version 1.8 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 15 | 12 | 1,3 | 11 | 11 | 1,0 |
| 0,5 MB | 10 | 13 | 0,8 | 55 | 17 | 3,2 |
| 1,0 MB | 18 | 18 | 1,0 | 178 | 33 | 5,4 |
| 5,0 MB | 25 | 84 | 0,3 | 2.223 | 91 | 24,4 |
| 10,0 MB | 47 | 143 | 0,3 | 8.821 | 169 | 52,2 |
| 100,0 MB | 445 | 1.428 | 0,3 | 1.270.292 | 1.696 | 749,0 |

#### Java 12: OpenJDK 64-Bit Server VM 12+33
#### SunJCE version 12 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 16 | 12 | 1,3 | 37 | 7 | 5,3 |
| 0,5 MB | 17 | 14 | 1,2 | 86 | 13 | 6,6 |
| 1,0 MB | 36 | 25 | 1,4 | 290 | 27 | 10,7 |
| 5,0 MB | 13 | 104 | 0,1 | 3.177 | 107 | 29,7 |
| 10,0 MB | 27 | 184 | 0,1 | 14.771 | 204 | 72,4 |
| 100,0 MB | 220 | 1.751 | 0,1 | 2.030.858 | 1.874 | 1083,7 |

#### Java 17: OpenJDK 64-Bit Server VM 17+35-2724
#### SunJCE version 17 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 22 | 16 | 1,4 | 19 | 9 | 2,1 |
| 0,5 MB | 23 | 13 | 1,8 | 79 | 21 | 3,8 |
| 1,0 MB | 30 | 26 | 1,2 | 178 | 28 | 6,4 |
| 5,0 MB | 15 | 115 | 0,1 | 1.740 | 134 | 13,0 |
| 10,0 MB | 27 | 185 | 0,1 | 6.314 | 214 | 29,5 |
| 100,0 MB | 237 | 1.745 | 0,1 | 572.887 | 1.980 | 289,3 |

#### Java 20.0.1: OpenJDK 64-Bit Server VM 20.0.1+9
#### SunJCE version 20 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 17 | 11 | 1,5 | 18 | 6 | 3,0 |
| 0,5 MB | 11 | 11 | 1,0 | 64 | 12 | 5,3 |
| 1,0 MB | 20 | 19 | 1,1 | 159 | 30 | 5,3 |
| 5,0 MB | 13 | 82 | 0,2 | 1.470 | 87 | 16,9 |
| 10,0 MB | 23 | 154 | 0,1 | 5.262 | 174 | 30,2 |
| 100,0 MB | 222 | 1.471 | 0,2 | 516.352 | 1.665 | 310,1 |
