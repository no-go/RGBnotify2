#include <SoftwareSerial.h>
#include "ssd1306xled.h"
#include "ssd1306xled.c"
#include "num2str.h"
#include "num2str.c"

// I2C ---------------------------- attiny85
// PB2 -> SCL on SSD1306 Board
// PB0 -> SDA on SSD1306 Board

const int rstPin = A0;

const int rxPin  =  PB4;
const int txPin  =  PB3;

int tick = 0;
char buff[90] = {'\0'};

SoftwareSerial mySerial(rxPin, txPin);

uint16_t vcc = 3700;

void readVcc() {
  // Read 1.1V reference against Vcc
  ADMUX = (0<<REFS0) | (12<<MUX0);
  delay(8); // Wait for Vref to settle
  ADCSRA |= (1<<ADSC); // Convert
  while (bit_is_set(ADCSRA,ADSC));
  vcc = ADCW;
  vcc = 1125300L / vcc; // Back-calculate AVcc in mV
}

void setup() {
  pinMode(rxPin,     INPUT);
  pinMode(rstPin,    INPUT);
  mySerial.begin(4800);
  //mySerial.println("AT+NAME=ATtiny Term");
  
  // 4 line with 21 chars
  _delay_ms(40);
  ssd1306_init();
  ssd1306_clear();
  ssd1306_setpos(0, 0);
  readVcc();
  ssd1306_numdec_font6x8(vcc);
  ssd1306_string_font6x8(" mV");
  ssd1306_setpos(0, 1);
  if (vcc > 3740) {
    ssd1306_string_font6x8("DEAD! BLE need <3.7 V");
    delay(3000);
  }
  ssd1306_setpos(0, 2);
  ssd1306_string_font6x8("I am a bluetooth");
  ssd1306_setpos(0, 3);
  ssd1306_string_font6x8("terminal.");
  delay(3000);
  ssd1306_clear();
  delay(200);
  ssd1306_setpos(0, 0);
}

void loop() {
  //while(mySerial.available()) {
  if (mySerial.available() > 0) {
    char readValue = (char) mySerial.read();
    if (readValue >= 32 && readValue <= 126) {
      buff[tick] = readValue;
      buff[tick+1] = '\0';
      tick++;    
      
      if (tick > 84) {
        tick = 1;
        ssd1306_clear();
        delay(100);
        buff[0] = readValue;
        buff[1] = '\0';
      }
    }
  }

  ssd1306_setpos(0, 0);
  ssd1306_string_font6x8(buff);
  delay(100);
}


