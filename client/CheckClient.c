#include <reg52.h>
#include "esp8266.h"
#include "stc52ser.h"

#define uchar unsigned char 
#define CMD_ACTION 4
#define CMD_SEAT_ID 5

uchar state = 0;
//��Ҫ�޸ĵ�Ϊ4��5��������0��ʼ�㣩,�ֱ����ʼ(0)/����(1)����������λid����0��ʼ�㣩
uchar send_data[SEND_DATA_SIZE] = {0xff, 0x06, 0x02, 0x01, 0x00, 0x00};
uchar isConnect = 0;


//���ӵ�wifi
void EventHandler_WifiConnected(){
    P1 &= 0xFE;
}

//���IP
void EventHandler_IpGot(){
    P1 &= 0xFD;
    ESP01_ConnectToUDPServer();
}

//���ӵ�������
void EventHandler_UDPServerConnected(){
  P1 &= 0xFB;
	isConnect = 1;
}

//��ʼ��
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
					send_data[CMD_ACTION] = now == 0 ? 1 : 0;//ȡ0/1
					send_data[CMD_SEAT_ID] = i;
					ESP01_SendUDPData(send_data);
					hasChanged = 1;
					Delay10ms(5);//��ֹ���͹���UDP�ϳ�һ��������
        }
    }
		if(hasChanged) {
			hasChanged = 0;
			state = temp;
			//P1 = state;
		}
	}
}