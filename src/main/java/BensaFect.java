import gamedata.furnidata.FurniData;
import gamedata.furnidata.furnidetails.FloorItemDetails;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import hotel.Hotel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


@ExtensionInfo(
        Title =  "BensaFect",
        Description =  "Automaticaly gives you Bensalem effects.",
        Version =  "1.0",
        Author =  "schweppes0x"
)


public class BensaFect extends ExtensionForm {

    public ToggleButton toggleButton;

    private Timer timer;
    private int itemId = -1;

    private Hotel hotel;
    private FurniData furniData;

    private HPacket placePacket;
    private HPacket usePacket;
    private HPacket pickUpPacket;

    @Override
    protected void initExtension() {
        super.initExtension();
        onConnect((host, i, s1, s2, hClient) -> {
            switch (host) {
                case "game-nl.habbo.com":
                    hotel = Hotel.NL;
                    break;
                case "game-br.habbo.com":
                    hotel = Hotel.COMBR;
                    break;
                case "game-tr.habbo.com":
                    hotel = Hotel.COMTR;
                    break;
                case "game-de.habbo.com":
                    hotel = Hotel.DE;
                    break;
                case "game-fr.habbo.com":
                    hotel = Hotel.FR;
                    break;
                case "game-fi.habbo.com":
                    hotel = Hotel.FI;
                    break;
                case "game-es.habbo.com":
                    hotel = Hotel.ES;
                    break;
                case "game-it.habbo.com":
                    hotel = Hotel.IT;
                    break;
                case "game-s2.habbo.com":
                    hotel = Hotel.SANDBOX;
                    break;
                default:
                    hotel = Hotel.COM;
                    break;
            }

            try {
                furniData = new FurniData(hotel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        intercept(HMessage.Direction.TOSERVER, "PlaceObject", (hMessage)->{

        });
        intercept(HMessage.Direction.TOCLIENT, "ObjectAdd", (hMessage1 -> {
            //Check if Totem Planet already found
            if(itemId != -1)
                return;

            //Save id's & coordinates
            int id = hMessage1.getPacket().readInteger();
            int tempitemId = hMessage1.getPacket().readInteger();
            int x = hMessage1.getPacket().readInteger();
            int y = hMessage1.getPacket().readInteger();

            // Check whether user placed correct item
            if(furniData.getFloorItemDetailsByTypeID(tempitemId).className.equals("totem_planet")){
                itemId = tempitemId;
                pickUpPacket = new HPacket("{out:PickupObject}{i:2}{i:"+id+"}");
                usePacket = new HPacket("{out:UseFurniture}{i:"+id+"}{i:0}");
                placePacket = new HPacket("{out:PlaceObject}{s:\"-"+id+" "+x+" "+y+" 0\"}");

                sendToServer(pickUpPacket);
                Platform.runLater(()->{
                    toggleButton.textProperty().setValue("Ready");
                    toggleButton.styleProperty().setValue("-fx-background-color:#AAFFAA");
                });
            }
        }));
    }

    public void toggleStart(ActionEvent actionEvent) {
        /*TODO implement start
          1. put item on ground (150ms) {out:PlaceObject}{s:"-itemId X Y 0"}
          2. use item (150ms) {out:UseFurniture}{i:itemId}{i:0}
          3. pickup (150ms) {out:PickupObject}{i:2}{i:itemId}
        * */

        if(itemId == -1)
            return;

        if(toggleButton.isSelected()){
            startSchedule();
        }else {
            stopSchedule();
        }
    }

    private void stopSchedule() {
        //stop & remove running tasks
        timer.cancel();
    }

    private void startSchedule() {
        //start tasks
        timer = new Timer();

        //schedule
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while(toggleButton.isSelected()){
                    sendToServer(placePacket);
                    try{Thread.sleep(20);}
                    catch (Exception e){

                    }
                    sendToServer(usePacket);

                    try{Thread.sleep(50);}
                    catch (Exception e){

                    }
                    sendToServer(pickUpPacket);
                }
            }
        }, 0, 150);
    }
}