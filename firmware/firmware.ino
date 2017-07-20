#include <SoftwareSerial.h>
#include "ssd1306xled.h"
#include "ssd1306xled.c"

// I2C ---------------------------- attiny85
// PB2 -> SCL on SSD1306 Board
// PB0 -> SDA on SSD1306 Board

const int rstPin = A0;

const int rxPin  =  PB4;
const int txPin  =  PB3;

char readValue = '\0';
int tick = 0;

SoftwareSerial mySerial(rxPin, txPin);

void setup() {
  pinMode(rxPin,     INPUT);
  pinMode(rstPin,    INPUT);
  mySerial.begin(4800);
  
  delay(40);
  ssd1306_init();
  ssd1306_clear();
  ssd1306_setpos(0, 0);
  // 4 line with 21 chars
  ssd1306_string_font6x8("I am a bluetooth terminal. Send a newline to clear me.");
  delay(3000);
  ssd1306_clear();
  ssd1306_setpos(0, 0);
}

void loop() {
  if (mySerial.available() > 0) {
    readValue = mySerial.read();
    if (readValue == '\n') {
      tick = 0;
      ssd1306_clear();
      ssd1306_setpos(0, 0);
    }
    ssd1306_string_font6x8(readValue);
    tick++;
    if (tick > 84) {
      tick = 0;
      ssd1306_clear();
      ssd1306_setpos(0, 0);
      ssd1306_string_font6x8(readValue);
    }
  }
  delay(100);
}


