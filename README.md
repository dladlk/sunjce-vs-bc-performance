# sunjce-vs-bc-performance
Compare performance of SunJCE Cipher for Peppol encryption algorithm vs Bouncy Castle

Algorithm URI: http://www.w3.org/2009/xmlenc11#aes128-gcm

Algorithm code: AES/GCM/NoPadding

## Java 1.8 SunJCE cipher decryption

![Chart](./blob/main/doc/chart.png)

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
1. But Java 21 solved this performance issue (although did not find any notes about it at https://www.oracle.com/java/technologies/javase/21all-relnotes.html)
1. Java 21 - **DO NOT SWITCH TO BouncyCastle** as SunJCE performs better!

## Result tables

All time in ms.

#### Java 1.8.0_372: OpenJDK 64-Bit Server VM 25.372-b07
#### SunJCE version 1.8 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 15 | 12 | 1,3 | 11 | 6 | 1,8 |
| 0,5 MB | 13 | 14 | 0,9 | 58 | 12 | 4,8 |
| 1,0 MB | 19 | 21 | 0,9 | 186 | 32 | 5,8 |
| 5,0 MB | 25 | 78 | 0,3 | 2.404 | 89 | 27,0 |
| 10,0 MB | 45 | 134 | 0,3 | 9.408 | 168 | 56,0 |
| 20,0 MB | 92 | 269 | 0,3 | 47.099 | 320 | 147,2 |
| 30,0 MB | 186 | 495 | 0,4 | 110.852 | 651 | 170,3 |
| 40,0 MB | 201 | 567 | 0,4 | 203.520 | 711 | 286,2 |
| 50,0 MB | 255 | 722 | 0,4 | 325.340 | 922 | 352,9 |
| 100,0 MB | 497 | 1.497 | 0,3 | 1.640.014 | 1.813 | 904,6 |

#### Java 12: OpenJDK 64-Bit Server VM 12+33
#### SunJCE version 12 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 20 | 14 | 1,4 | 46 | 8 | 5,8 |
| 0,5 MB | 20 | 15 | 1,3 | 108 | 17 | 6,4 |
| 1,0 MB | 47 | 24 | 2,0 | 359 | 36 | 10,0 |
| 5,0 MB | 19 | 99 | 0,2 | 4.439 | 106 | 41,9 |
| 10,0 MB | 36 | 185 | 0,2 | 21.150 | 198 | 106,8 |
| 20,0 MB | 69 | 377 | 0,2 | 100.683 | 401 | 251,1 |
| 30,0 MB | 121 | 543 | 0,2 | 213.957 | 569 | 376,0 |
| 40,0 MB | 138 | 688 | 0,2 | 344.498 | 726 | 474,5 |
| 50,0 MB | 144 | 827 | 0,2 | 549.558 | 869 | 632,4 |
| 100,0 MB | 323 | 1.659 | 0,2 | 2.179.325 | 1.743 | 1250,3 |

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

#### Java 21: Java HotSpot(TM) 64-Bit Server VM 21+35-LTS-2513
#### SunJCE version 21 vs. BC version 1.7
| Size | Encrypt Sun | Encrypt BC | Encrypt Sun/BC | Decrypt Sun | Decrypt BC | Decrypt Sun/BC |
| --- | --- | --- | --- | --- | --- | --- |
| 0,1 MB | 19 | 11 | 1,7 | 11 | 7 | 1,6 |
| 0,5 MB | 15 | 13 | 1,2 | 11 | 16 | 0,7 |
| 1,0 MB | 22 | 16 | 1,4 | 21 | 23 | 0,9 |
| 5,0 MB | 14 | 72 | 0,2 | 98 | 83 | 1,2 |
| 10,0 MB | 25 | 135 | 0,2 | 184 | 155 | 1,2 |
| 20,0 MB | 56 | 305 | 0,2 | 360 | 317 | 1,1 |
| 30,0 MB | 81 | 413 | 0,2 | 538 | 460 | 1,2 |
| 40,0 MB | 104 | 561 | 0,2 | 733 | 670 | 1,1 |
| 50,0 MB | 140 | 714 | 0,2 | 903 | 787 | 1,1 |
| 100,0 MB | 255 | 1.435 | 0,2 | 1.851 | 1.533 | 1,2 |
