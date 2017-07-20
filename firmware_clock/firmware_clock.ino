#include <SoftwareSerial.h>
#include "ssd1306xled.h"
#include "ssd1306xled.c"
#include "num2str.h"
#include "num2str.c"

// I2C ---------------------------- attiny85
// PB2 -> SCL on SSD1306 Board
// PB0 -> SDA on SSD1306 Board

#define SHOWCLOCKSEC  10

const int rstPin = A0;

const int rxPin  =  PB4;
const int txPin  =  PB3;

uint16_t startClock = 0;

byte quad = 0;
uint16_t seconds = 0;
uint16_t minu    = 0;
int      tick    = 0;
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
  delay(40);
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

  // sets the timer on 250ms ?!?!
  cli();
  TCCR1=0;
  TCNT1 = 0;                  //zero the timer
  GTCCR = _BV(PSR1);          //reset the prescaler
  OCR1A=243;           //contador CTC, tiene un tamaño de 2^8-1=255
  OCR1C=243;
  TCCR1 |= (1<<CTC1);  //el analogo a WGM12 en los atmega
  TCCR1 |= (1<<CS10);//
  TCCR1 |= (1<<CS11);//
  TCCR1 |= (1<<CS12);
  TCCR1 |= (1<<CS13);//   CS10=1, CS11=1, CS12=1, CS13=1   ==> prescaler=16384 (datasheet attiny85)
                     // T=1/(f/prescaler)==> 16384/8MHz = 2048us
                     // 2048us*CTC=2048us*244= 500 ms          
  TIMSK=(1<<OCIE1A); // para habilitar la comparacion Output Compare A Match (vector interrupcion)
  sei();
}

void loop() {
  //while(mySerial.available()) {
  if (mySerial.available() > 0) {
    char readValue = (char) mySerial.read();
    if (readValue >= 32 && readValue <= 126) {
      startClock = 0;
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

  if (startClock >= SHOWCLOCKSEC) {
    startClock = SHOWCLOCKSEC;
    if (minu<10) ssd1306_string_font6x8("0");
    ssd1306_numdec_font6x8(minu);
    ssd1306_string_font6x8(":");
    if (seconds<10) ssd1306_string_font6x8("0");
    ssd1306_numdec_font6x8(seconds);
    ssd1306_string_font6x8(" ");
  }
  
  ssd1306_string_font6x8(buff);
  delay(100);
}

ISR(TIMER1_COMPA_vect) {
  if (quad == 4) {
    startClock++;
    seconds++;
    quad = 0;
  } else {
    quad++;
  }
  if (seconds == 60) {
    minu++; seconds = 0;
  }
}

