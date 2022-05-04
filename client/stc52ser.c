#include <reg52.h>
#include "ASCII.h"
#include "stc52ser.h"

//Byte Received Event
void (*SerialPort_Event_ByteReceived)(unsigned char byte);

//initialize registers pertinent to serial port
void SerialPort_Init_Low(){
    //set and run Timer1
    //mode2: 8bit, auto reload initial value
    //9600bps and 11.0592MHz => 0xfd(initial value)
    TMOD = 0x20;
    TH1 = 0xfd;
    TL1 = 0xfd;
    TR1 = 1;
    
    //set serial port configuration and enable receive
    //mode1: asyc 10bit(8 data bit), alterable baud rate
    SM0 = 0;
    SM1 = 1;
    REN = 1;
    
    //set interruption
    //enable all and serial port interruption
    EA = 1;
    ES = 1;
}

//Send a byte
void SerialPort_SendByte(unsigned char byte){
    ES = 0;
    SBUF = byte;
    while(!TI);
    // transmit interrupt
    TI = 0;
    ES = 1;
}

//Send a data of byte sequence end by 'EOT'
void SerialPort_SendData(unsigned char* bytes){
    int i = 0;
    while(bytes[i] != EOT){
        SerialPort_SendByte(bytes[i]);
        i++;
    }
}


//Occured when byte received
void receivedInterruped() interrupt 4 {
    TR0 = 0;
		(*SerialPort_Event_ByteReceived)(SBUF);
    while(!RI);
    RI = 0;
}