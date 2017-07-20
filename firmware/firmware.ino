#include <SoftwareSerial.h>
#include "ssd1306xled.h"
#include "ssd1306xled.c"

// I2C ---------------------------- attiny85
// PB2 -> SCL on SSD1306 Board
// PB0 -> SDA on SSD1306 Board

const int rstPin = A0;

const int rxPin  =  PB4;
const int txPin  =  PB3;

int tick = 0;
char buff[85] = {'\0'};

SoftwareSerial mySerial(rxPin, txPin);

// does it works?!?
char umlReplace(char inChar) {
  if (inChar <= -62) return ' ';
  
  if (inChar == -97) {
    inChar = 224; // ß
  } else if (inChar == -80) {
    inChar = 248; // °
  } else if (inChar == -67) {
    inChar = 171; // 1/2
  } else if (inChar == -78) {
    inChar = 253; // ²
  } else if (inChar == -92) {
    inChar = 132; // ä
  } else if (inChar == -74) {
    inChar = 148; // ö
  } else if (inChar == -68) {
    inChar = 129; // ü
  } else if (inChar == -124) {
    inChar = 142; // Ä
  } else if (inChar == -106) {
    inChar = 153; // Ö
  } else if (inChar == -100) {
    inChar = 154; // Ü
  } else if (inChar == -85) {
    inChar = 0xAE; // <<
  } else if (inChar == -69) {
    inChar = 0xAF; // >>
  }
  return inChar;  
}

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
  //while(mySerial.available()) {
  if (mySerial.available() > 0) {
    char readValue = (char) mySerial.read();
    buff[tick] = umlReplace(readValue);
    buff[tick+1] = '\0';
    
    if (readValue == '\n') {
      tick = 0;
      ssd1306_clear();
      _delay_ms(200);
      buff[0] = '\0';
    } else {
      tick++;
    }
    
    if (tick > 84) {
      tick = 1;
      ssd1306_clear();
      _delay_ms(200);
      buff[0] = umlReplace(readValue);
      buff[1] = '\0';
    }
  }
  ssd1306_setpos(0, 0);
  ssd1306_string_font6x8(buff);
}


