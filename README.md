# JAvanza
 
Terminal application created for daily viewing of your stock(s) portfolio on Avanza.

## Usage

To be able to send requests to Avanza your secret key will be needed. And username/password...
1. To retrieve the key you first need to visit Settings -> Website-settings -> Two Factor Authentication (Reactivate if you have done this before).
2. When you get to the QR code press "Cant see QR", grab the code generated and complete the final steps.
3. This code will be used in the WEBHOOK variable, and used to get OTP for authentication.
4. Generate a webhook, and you are done.




## Libraries

```bash
<dependency>
    <groupId>club.minnced</groupId>
    <artifactId>discord-webhooks</artifactId>
    <version>0.5.4-rc</version>
</dependency>
```
```bash
<dependency>
    <groupId>com.github.bastiaanjansen</groupId>
    <artifactId>otp-java</artifactId>
    <version>1.1.1</version>
</dependency>
```
```bash
<dependency>
    <groupId>com.github.eitraz</groupId>
    <artifactId>avanza-api</artifactId>
    <version>1.0</version>
</dependency>
```
