SSLUnpinning
========

Android app to bypass SSL certificate validation (Certificate Pinning).

Description
-----------

If you need intercept the traffic from one app who use certificate pinning, with a tool like Burp Proxy, the SSLUnpinning help you with this hard work! 
The SSLUnpinning through Cydia Substrate, make severous hooks in SSL classes to bypass the certificate verifications for one specific app, then you can intercept all your traffic.

Usage
---------------

* install Cydia Substrate in your test device (rooted device);
https://play.google.com/store/apps/details?id=com.saurik.substrate
* Download the APK available here https://github.com/ac-pm/SSLUnpinning or clone the project and compile;
* Install SSLUnpinning.apk on a device with Cydia Substrate:

        adb install SSLUnpinning.apk

* SSLUnpinning will list the applications to choose from which will be bypassed;
* Ok! Now you can intercept all traffic from the chosen app.

### How to uninstall

        adb uninstall SSLUnpinning.apk
        
License
-------

See ./LICENSE.

Author
-------

ACPM
