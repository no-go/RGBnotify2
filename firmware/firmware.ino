#include <SoftwareSerial.h>
const int redPin    =  0;
const int greenPin  =  4;
const int bluePin   =  1;
const int rstPin    = A0;

const int rxPin     =  2;
const int txPin     =  3;

char readValue = '0';

int red   = 254;
int green = 254;
int blue  = 254;

SoftwareSerial mySerial(rxPin, txPin);

void setup() {
  pinMode(rxPin,     INPUT);
  pinMode(rstPin,    INPUT);
  pinMode(redPin,   OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin,  OUTPUT);

  mySerial.begin(4800);
  //mySerial.begin(9600);

  // Configure counter/timer0 for fast PWM on PB0 and PB1
  TCCR0A = 3<<COM0A0 | 3<<COM0B0 | 3<<WGM00;
  TCCR0B = 0<<WGM02 | 3<<CS00; // Optional; already set
  // Configure counter/timer1 for fast PWM on PB4
  GTCCR = 1<<PWM1B | 3<<COM1B0;
  TCCR1 = 3<<COM1A0 | 7<<CS10;

  analogWrite(  redPin, red);
  analogWrite(greenPin, green);
  analogWrite( bluePin, blue);
}

int tick = 0;

void loop() {
  if (mySerial.available() > 0) {
    readValue = mySerial.read();

    if (readValue != '\n') {

      if (readValue < 65) {
        
        // set white via '0' - '9' chars
        red = green = blue = 254 - 25 * (readValue - 48);
        analogWrite(redPin, red);
        analogWrite(greenPin, green);
        analogWrite(bluePin, blue);

      } else {
        // ascii 'A' to 1
        readValue -= 65;

        if (readValue == 0) {
          red = 254;
          green = 254;
          blue = 254;
        } else if (readValue >= 1 && readValue < 10) {
          red = 254 - 28 * (readValue-1);
        } else if (readValue >= 10 && readValue < 19) {
          green = 254 - 28 * (readValue-10);
        } else if (readValue >= 19 && readValue < 28) {
          blue = 254 - 28 * (readValue-19);
        }

        if (red < 4) {
          digitalWrite(redPin, LOW);  
        } else {
          analogWrite(redPin, red);          
        }
        if (green < 4) {
          digitalWrite(greenPin, LOW);  
        } else {
          analogWrite(greenPin, green);          
        }
        if (blue < 4) {
          digitalWrite(bluePin, LOW);  
        } else {
          analogWrite(bluePin, blue);          
        }
      }
    }
  }
  delay(100);
  tick++;
  if(tick>=300) {
    tick=0;
    mySerial.println("Ping");
  }
}


