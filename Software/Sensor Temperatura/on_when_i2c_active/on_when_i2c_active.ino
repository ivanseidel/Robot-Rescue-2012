/*
  Blink
  Turns on an LED on for one second, then off for one second, repeatedly.
 
  This example code is in the public domain.
 */
int led = 8;
int i2c = A5;

void setup() {                
  // initialize the digital pin as an output.
  // Pin 13 has an LED connected on most Arduino boards:
  pinMode(led, OUTPUT);     
  pinMode(stat, INPUT);     
}

void loop() {
  if(digitalRead(stat) == LOW){
    digitalWrite(pin, HIGH);   // set the LED on
    delay(50);              // wait for a second
    digitalWrite(pin, LOW);    // set the LED off
  }
}
