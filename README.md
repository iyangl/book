### 对称加密

通过**加密算法**使用**密钥**对**源数据**进行加密得到**密文**。

通过**解密算法**使用**密钥**对**密文**进行解密得到**源数据**。

* DES、AES



### 非对称加密

通过**加密算法**使用**加密密钥**对**源数据**进行加密得到**密文**。

通过**加密算法**使用**解密密钥**对**密文**进行加密得到**源数据**。

* RSA

##### 例:

* 客户端使用`加密密钥A(公钥)`对数据进行加密，发送到服务端。
* 服务端使用`解密密钥A(私钥)`对数据进行解密，得到源数据。
* 服务端使用`加密密钥B(公钥)`对应答进行加密，发送给客户端。
* 客户端使用`解密密钥B(私钥)`对应答进行解密，得到应答数据。

#### 签名

**私钥**可以**解密**出**公钥加密**的数据。**公钥**也可以**解密**出**私钥加密**的数据

* DSA

> 椭圆曲线算法，可以通过私钥计算出公钥。

##### 例:

* 客户端使用`加密密钥A(公钥)`对数据进行加密，使用`解密密钥B(私钥)`进行签名，一起发送给服务端。
* 服务端使用`解密密钥A(私钥)`对密文进行解密，得到源数据。
* 服务端使用`加密密钥B(公钥)`对签名进行验证，得到源数据。
* 解密与验证得到的源数据相同，服务端认为该数据来自客户端，可以信任。

#### Hash 算法

又称作散列算法或摘要算法。通过 Hash 算法将源数据映射为一段长度固定的值，具有单向不可还原的特性，对源数据进行**微小的改动**也会导致 **Hash 值**发生变动。

* MD5、SHA1

##### 例:

上一个栗子中，由于源数据的长度可能很长，直接对源数据签名的成本较高。
因为理论上的 Hash 算法是无碰撞的（实际中很难实现），可以使用 Hash 算法对源数据生成**消息摘要**，对消息摘要进行签名。

* 客户端对源数据使用 `Hash 算法`生成`消息摘要`。
* 客户端使用`加密密钥A(公钥)`对数据进行加密，使用`解密密钥B(私钥)`对`消息摘要`进行签名，一起发送给服务端。
* 服务端使用`加密密钥B(公钥)`对签名进行验证，得到`消息摘要`。
* 服务端使用`解密密钥A(私钥)`对密文进行解密，得到源数据。
* 服务端对源数据使用 `Hash 算法` 生成`消息摘要`，与客户端传来的`消息摘要`进行对比。数据一致则认为数据来源合法。

### 对称加密与非对称加密结合使用

对称加密有秘钥泄露的风险但是加解密速度快。   
非对称加密秘钥保存更安全但是加解密速度比对称加密慢。

使用随机生成的对称加密秘钥A对源数据进行加密，使用非对称加密密钥(公钥)对A进行加密，将密文与加密后的秘钥一起传给服务端。
服务端使用非对称解密秘钥(私钥)对A解密，使用A对密文解密得到源数据。

##### 例:

* 对源数据进行 `Hash 运算`，生成消息摘要。
* `随机生成`加密密钥，使用加密密钥对源数据进行`对称加密`。
* 使用公钥对消息摘要进行`非对称加密`，生成数字签名。
* 使用公钥对加密密钥进行`非对称加密`。
* 将数字签名、密文、加密后的加密密钥发送给服务端。
* 服务端使用私钥对数字签名进行`非对称解密`，得到消息摘要。
* 服务端使用私钥对加密后的加密密钥进行`非对称解密`，得到`对称加密`使用的加密密钥。
* 服务端使用加密密钥对密文进行`对称解密`，得到源数据。
* 服务端对源数据进行 `Hash 运算`，生成消息摘要。
* 服务端对比消息摘要，一致则认为数据合法。