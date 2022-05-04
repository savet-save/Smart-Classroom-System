#include <reg52.h>
#include "esp8266.h"
#include "stc52ser.h"

#define uchar unsigned char 
#define CMD_ACTION 4
#define CMD_SEAT_ID 5

uchar state = 0;
//需要修改的为4和5（从序列0开始算）,分别代表开始(0)/结束(1)上座，和座位id（从0开始算）
uchar send_data[SEND_DATA_SIZE] = {0xff, 0x06, 0x02, 0x01, 0x00, 0x00};
uchar isConnect = 0;


//连接到wifi
void EventHandler_WifiConnected(){
    P1 &= 0xFE;
}

//获得IP
void EventHandler_IpGot(){
    P1 &= 0xFD;
    ESP01_ConnectToUDPServer();
}

//连接到服务器
void EventHandler_UDPServerConnected(){
  P1 &= 0xFB;
	isConnect = 1;
}

//初始化
void init(){
    ESP01_Event_WifiConnected = &EventHandler_WifiConnected; 
    ESP01_Event_IpGot = &EventHandler_IpGot;
    ESP01_Event_UDPServerConnected = &EventHandler_UDPServerConnected; 
    ESP01_Init();
}


void main(void) {
	uchar i, before, now, temp, hasChanged = 0;
	init();
	
	while(!isConnect);
	state = P2;
	while(1) {
		temp = P2;
    for(i = 0; i < 8; i++) {
        before = state & ( 0x1 << i);
        now = temp & ( 0x1 << i);
        if(now != before) {
					//send to server data
					send_data[CMD_ACTION] = now == 0 ? 1 : 0;//取0/1
					send_data[CMD_SEAT_ID] = i;
					ESP01_SendUDPData(send_data);
					hasChanged = 1;
					Delay10ms(5);//防止发送过快UDP合成一个包发送
        }
    }
		if(hasChanged) {
			hasChanged = 0;
			state = temp;
			//P1 = state;
		}
	}
}