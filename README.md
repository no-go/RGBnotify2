# RGB Notify 2

THIS BRANCH HAS AN I2C OLED INSTEAD OF A SINGLE RGB LED. YOU CAN USE THE ATTINY85 AS TERMINAL.

![logo](app/src/main/res/drawable-hdpi/icon.png)

This App is part of a small ATtin85 project. It sends a specific LED color code to a ATtiny85 and nRF51822 (UART) device:

  - 0..9 - black to white
  - A..J - red value
  - L..S - green value
  - T..Z - blue value

Some "Disco modes" are added, too! You can send text messages, too!

## App

<a href="https://play.google.com/store/apps/details?id=click.dummer.rgbnotify2" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="90"/></a>

You can optionaly get a signed APK from here: [APK](https://raw.githubusercontent.com/no-go/RGBnotify2/master/app/app-release.apk)

## The Firmware

[firmware for Arduino IDE](https://raw.githubusercontent.com/no-go/RGBnotify2/oled/firmware/firmware.ino)

## Circuit (OLED Terminal)

ATTENTION! the Lipo has sometimes more than 3.7 V. The nRF51822 Chip
NEEDS 3.3 - 3.6 V. Thus, this circuit may brick your bluetooth module!!!

![Circuit with ATtin85, nRF51822 and OLED](_img/circuit.png)

![Picture of device](_img/action.jpg)

## Bugs

on 20. July 2017 : it is still a bit buggy.

## License

I publish everything under the free BSD-3 License.

## Privacy Policy

### Personal information.

Personal information is data that can be used to uniquely identify or contact a single person. I DO NOT collect, transmit, store or use any personal information while you use this app.

### Non-Personal information.

I DO NOT collect non-personal information like user's behavior:

 -  to solve App problems
 -  to show personalized ads

The google play store collect non-personal information such as the data of install (country and equipment).

### Bluetooth and Position access

Google Play requires me to disclose this App will take access to your position. Your position is not send to any one. The Bluetooth Low Energy needs access to position data - I do not know, why. Every information you send via bluetooth may be
sniffed by hackers.

### Privacy Questions.

If you have any questions or concerns about my Privacy Policy or data processing, please contact me.
