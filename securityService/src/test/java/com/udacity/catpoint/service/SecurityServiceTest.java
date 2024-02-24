package com.udacity.catpoint.service;

import com.udacity.catpoint.data.*;
import junit.framework.Assert;
import org.imageService.FakeImageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SecurityServiceTest {
    @Mock
    private FakeImageService imageService;

    @Mock
    private BufferedImage bufferedImage;

    private Sensor doorSensor;
    SecurityService securityService1;
    @BeforeEach
    void setUp() {
        securityService1 = new SecurityService(new PretendDatabaseSecurityRepositoryImpl(), imageService);
        doorSensor = new Sensor("mock",SensorType.DOOR);
    }
    @Test
    void alarmArmed_sensorBecomesActive_systemPendingAlarmStatus(){
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.changeSensorActivationStatus(doorSensor, true);
        Assert.assertEquals(AlarmStatus.PENDING_ALARM, securityService1.getAlarmStatus());
    }

    @Test
    void alarmArmed_sensorBecomesActive_systemPendingAlarmStatus2(){
        securityService1.setArmingStatus(ArmingStatus.ARMED_AWAY);
        securityService1.changeSensorActivationStatus(doorSensor, true);
        Assert.assertEquals(AlarmStatus.PENDING_ALARM, securityService1.getAlarmStatus());
    }



    @Test
    void alarmArmed_sensorBecomesActive_systemPendingAlarm_systemAlarmStatusAlarm(){
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService1.changeSensorActivationStatus(doorSensor, true);
        Assert.assertEquals(AlarmStatus.ALARM, securityService1.getAlarmStatus());
    }

    @Test
    void alarmPending_sensorinactive_systemNoAlarmStatus(){
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        doorSensor.setActive(true);
        securityService1.changeSensorActivationStatus(doorSensor, false);
        Assert.assertEquals(AlarmStatus.NO_ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("4. If alarm is active, change in sensor state should not affect the alarm state.")
    void alarmActive_changeinSensorState_notAffectAlarmState(){
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.ALARM);
        doorSensor.setActive(true);
        securityService1.changeSensorActivationStatus(doorSensor, false);
        Assert.assertEquals(AlarmStatus.ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("5. If a sensor is activated while already active and the system is in pending state, change it to alarm state")
    void sensorIsActivated_whileActive_changeToAlarmState(){
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        doorSensor.setActive(true);
        securityService1.changeSensorActivationStatus(doorSensor, true);
        Assert.assertEquals(AlarmStatus.ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("6. If a sensor is deactivated while already inactive, make no changes to the alarm state.")
    void sensorIsDeactivated_whileInactive_noChangeToAlarmState(){
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService1.changeSensorActivationStatus(doorSensor, false);
        Assert.assertEquals(AlarmStatus.PENDING_ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("7. If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.")
    void imageServiceIdentifiesCatImage_whileSystemIsArmedHome_putSystemInAlarmStatus(){
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService1.processImage(bufferedImage);
        Assert.assertEquals(AlarmStatus.ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("8. If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.")
    void imageServiceNotIdentifiesCatImage_changesStatusToNoAlarm_whileSensorsNotActive(){
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService1.setAlarmStatus(AlarmStatus.ALARM);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService1.processImage(bufferedImage);
        Assert.assertEquals(AlarmStatus.NO_ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("9. If the system is disarmed, set the status to no alarm.")
    void ifSystemDisarmed_setStatusToNoAlarm(){
        securityService1.addSensor(doorSensor);
        securityService1.setAlarmStatus(AlarmStatus.ALARM);
        securityService1.setArmingStatus(ArmingStatus.DISARMED);
        Assert.assertEquals(AlarmStatus.NO_ALARM, securityService1.getAlarmStatus());
    }

    @Test
    @DisplayName("10. If the system is armed, reset all sensors to inactive.")
    void IfSystemIsArmed_resetSensorsToInactive(){
        doorSensor.setActive(true);
        securityService1.addSensor(doorSensor);
        securityService1.setArmingStatus(ArmingStatus.DISARMED);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        Assertions.assertFalse(securityService1.getSensors().stream().findFirst().orElseThrow().getActive());
    }

    @Test
    @DisplayName("11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.  ")
    void IfSystemIsArmedHome_whileCameraShowsCat_alarmStatusAlarm(){
        securityService1.addSensor(doorSensor);
        securityService1.setAlarmStatus(AlarmStatus.ALARM);
        securityService1.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService1.processImage(bufferedImage);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService1.getAlarmStatus());
    }

}