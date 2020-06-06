
/**
 * AI Player
 * 
 * @author Sam Cavenagh 
 * @version 5/12/02
 * 
 * Website: http://home.pacific.net.au/~cavenagh/SH/
 * Email: cavenaghweb@hotmail.com
 */
public class GameAI
{

    boolean seven;
    boolean nine;

    public GameAI(boolean seven, boolean nine)
    {
    this.seven = seven;
    this.nine = nine;
    }

    public String basicMove(Hand hand, Card[] pile, int pilelength)
    {
    //determining top card
    Card top;
    if(pile[0] == null){
        return playlowest(hand, 15, null);
    }else if(nine == true && pile[0].getValue() == 9){
        int topcount = 0;
        for(int i = 0; i < 52; i++){
            if(pile[i] == null){
                return playlowest(hand, 15, null);
            }
            if(pile[i].getValue() == 9)
                topcount++;
            else
                break;
        }
        top = pile[topcount];
    }else
        top = pile[0];

    if(seven == true && top.getValue() == 7){
        return playlowest(hand, 6, top);
    }else{//normal play
        return playhigherthan(hand, top);
    }
    }

    private String playlowest(Hand hand, int lowest, Card card)
    {
    //System.out.println("PlayLowest - lower than:" + lowest);
    int lowestcount = 0;//number of cards with lowest value
    int tencount = 0;
    int twocount = 0;
    int ninecount = 0;
    if(hand.length() > 1){//if cards in hand
        //determining which card to play
        for(int n = 0; n < hand.length() - 1; n++){
            if(nine == true && hand.getCard(n).getValue() == 9){
                ninecount++;
            }else if(hand.getCard(n).getValue() == 10){
                tencount++;
            }else if(hand.getCard(n).getValue() == 2){
                twocount++;
            }else if(hand.getCard(n).getValue() < lowest){
                    lowest = hand.getCard(n).getValue();
                    lowestcount = 1;
            }else if(hand.getCard(n).getValue() == lowest){
                    lowestcount++;
            }
        }
        //playing card selected
        if(lowestcount == 0){//no card other than 10, 2 and 9 (if nine is true) or player must pickup

            if(twocount != 0)
                return commandcreator("turn:", hand, 2, twocount);
            else if(ninecount != 0)
                return commandcreator("turn:", hand, 9, ninecount);
            else if(tencount != 0)
                return commandcreator("turn:", hand, 10, tencount);
            else
                return ("turn:pickup:");
        
        }else{//play as many as there is of the lowest card

            return commandcreator("turn:", hand, lowest, lowestcount);

        }
    }else if(hand.isFaceUp()){
        //determining which card to play
        for(int n = 0; n < 3; n++){
            if(hand.getFaceUp(n) != null){
                if(nine == true && hand.getFaceUp(n).getValue() == 9){
                    ninecount++;
                }else if(hand.getFaceUp(n).getValue() == 10){
                    tencount++;
                }else if(hand.getFaceUp(n).getValue() == 2){
                    twocount++;
                }else if(hand.getFaceUp(n).getValue() < lowest){
                        lowest = hand.getFaceUp(n).getValue();
                        lowestcount = 1;
                }else if(hand.getFaceUp(n).getValue() == lowest){
                        lowestcount++;
                }
            }
        }
        //playing card selected
        if(lowestcount == 0){//no card other than 10, 2 and 9 (if nine is true) or player must pickup

            if(twocount != 0)
                return commandcreatorFU("turn:faceup:", hand, 2, twocount);
            else if(ninecount != 0)
                return commandcreatorFU("turn:faceup:", hand, 9, ninecount);
            else if(tencount != 0)
                return commandcreatorFU("turn:faceup:", hand, 10, tencount);
            else
                return ("turn:pickup:");
        
        }else{//play as many as there is of the lowest card

            return commandcreatorFU("turn:faceup:", hand, lowest, lowestcount);

        }
    }else
        return facedown(hand, card);
    }


    private String playhigherthan(Hand hand, Card card)
    {
    //System.out.println("Play higher than: " + card.getValue());
    int lowest = 15;
    int lowestcount = 0;//number of cards with lowest value
    int tencount = 0;
    int twocount = 0;
    int ninecount = 0;
    if(hand.length() > 1){//if cards in hand
        //determining which card to play
        for(int n = 0; n < hand.length() - 1; n++){
            //System.out.println("Card " + n + " = " + hand.getCard(n).getValue());
            if(nine == true && hand.getCard(n).getValue() == 9){
                ninecount++;
            }else if(hand.getCard(n).getValue() == 10){
                tencount++;
            }else if(hand.getCard(n).getValue() == 2){
                twocount++;
            }else if(hand.getCard(n).getValue() < lowest && hand.getCard(n).getValue() >= card.getValue()){
                    lowest = hand.getCard(n).getValue();
                    lowestcount = 1;
            }else if(hand.getCard(n).getValue() == lowest){
                    lowestcount++;
            }
        }
        //playing card selected
        if(lowestcount == 0){//no card other than 10, 2 and 9 (if nine is true) or player must pickup

            if(twocount != 0)
                return commandcreator("turn:", hand, 2, twocount);
            else if(ninecount != 0)
                return commandcreator("turn:", hand, 9, ninecount);
            else if(tencount != 0)
                return commandcreator("turn:", hand, 10, tencount);
            else
                return ("turn:pickup:");
        
        }else{//play as many as there is of the lowest card

            return commandcreator("turn:", hand, lowest, lowestcount);

        }
    }else if(hand.isFaceUp()){
        //determining which card to play
        for(int n = 0; n < 3; n++){
            if(hand.getFaceUp(n) != null){
                if(nine == true && hand.getFaceUp(n).getValue() == 9){
                    ninecount++;
                }else if(hand.getFaceUp(n).getValue() == 10){
                    tencount++;
                }else if(hand.getFaceUp(n).getValue() == 2){
                    twocount++;
                }else if(hand.getFaceUp(n).getValue() < lowest && hand.getFaceUp(n).getValue() >= card.getValue()){
                        lowest = hand.getFaceUp(n).getValue();
                        lowestcount = 1;
                }else if(hand.getFaceUp(n).getValue() == lowest){
                        lowestcount++;
                }
            }
        }
        //playing card selected
        if(lowestcount == 0){//no card other than 10, 2 and 9 (if nine is true) or player must pickup

            if(twocount != 0)
                return commandcreatorFU("turn:faceup:", hand, 2, twocount);
            else if(ninecount != 0)
                return commandcreatorFU("turn:faceup:", hand, 9, ninecount);
            else if(tencount != 0)
                return commandcreatorFU("turn:faceup:", hand, 10, tencount);
            else
                return ("turn:pickup:");
        
        }else{//play as many as there is of the lowest card

            return commandcreatorFU("turn:faceup:", hand, lowest, lowestcount);

        }
    }else
        return facedown(hand, card);
    }

    private String commandcreator(String command, Hand hand, int value, int number)
    {
    if(number == 0)
        return(command.concat("pickup:"));
    if(number > 1)
    command = command.concat("multi:" + number + ":");
    int addedcount = 0;
        for(int i = 0; i < hand.length() - 1; i++)
            if(hand.getCard(i).getValue() == value){
                addedcount++;
                command = command.concat( hand.getCard(i).getNumber() +":");
                if(addedcount == number)
                    break;
            }
    return command;
    }

    //FU for Face Up
    private String commandcreatorFU(String command, Hand hand, int value, int number)
    {
    if(number == 0)
        return(command.concat("pickup:"));
    if(number > 1)
    command = command.concat("multi:" + number + ":");
    int addedcount = 0;
        for(int i = 0; i < 3; i++)
            if(hand.getFaceUp(i) != null)
            if(hand.getFaceUp(i).getValue() == value){
                addedcount++;
                command = command.concat( hand.getFaceUp(i).getNumber() +":");
                if(addedcount == number)
                    break;
            }
    return command;
    }

    private String facedown(Hand hand, Card card)
    {
    String command;
        for(int n = 0; n < 3; n++)
            if(hand.getFaceDown(n) != null){
                if(card == null){
                    command = "turn:facedown:" + hand.getFaceDown(n).getNumber() + ":";
                    return command;
                }else if(seven == true && card.getValue() == 7){
                    if(hand.getFaceDown(n).getValue() < 7){
                        command = "turn:facedown:" + hand.getFaceDown(n).getNumber() + ":";
                        return command;
                    }else{
                        command = "turn:facedown:pickup:" + hand.getFaceDown(n).getNumber() + ":";
                        return command;
                    }
                }else if(hand.getFaceDown(n).getValue() == 9 && nine){
                    command = "turn:facedown:" + hand.getFaceDown(n).getNumber() + ":";
                    return command;
                }else if(hand.getFaceDown(n).getValue() == 2 || hand.getFaceDown(n).getValue() == 10){
                    command = "turn:facedown:" + hand.getFaceDown(n).getNumber() + ":";
                    return command;
                }else if(hand.getFaceDown(n).getValue() >= card.getValue()){
                    command = "turn:facedown:" + hand.getFaceDown(n).getNumber() + ":";
                    return command;
                }else{
                    command = "turn:facedown:pickup:" + hand.getFaceDown(n).getNumber() + ":";
                    return command;
                }
                
            }
    return "error";
    }
    
    public String swap(Hand hand, boolean nine)
    {
    //Stores all cards that can be swapped
    Card canbeswapped[] = new Card[6];
    
    //placing cards into canbeswapped in acending order
    int count = 0;//counts how many cards have already been added to canbeswapped
    Card card = hand.getCard(0);
    int cardvalue = 0;
    for(int f = 0; f < 6; f++){
        if(f < 3)//adding cards from in hand
            card = hand.getCard(f);
        if(f >= 3)//adding faceup cards
            card = hand.getFaceUp(5 - f);
            
        for(int n = 0; n < 6; n++){
            if(card == null)
                return "error";
            cardvalue = card.getValue();
            //resetting value for important cards like 2's and 10's
            if(cardvalue == 2)
                cardvalue = 15;
            if(cardvalue == 9 && nine)
                cardvalue = 16;
            if(cardvalue == 10)
                cardvalue = 16;
                
            if(canbeswapped[n] == null){
                canbeswapped[n] = card;
                break;
            }else if(canbeswapped[n].getValue() >= cardvalue){
                Card temp = canbeswapped[n];
                canbeswapped[n] = card;
                for(int s = count; s >= n + 1 ; s--)
                    if(s + 1 < 6)
                        canbeswapped[s + 1] = canbeswapped[s];
                    canbeswapped[n + 1] = temp;
                    break;
            }
        }
    count++;
    }
    //creating command string
    String command = "swap:";
    for(int n = 0; n < 6; n++)
        command = command.concat( canbeswapped[n].getNumber() + ":");
  
    return command;
    }

}
