package zigbotplayer;

import battlecode.common.GameActionException;
import battlecode.common.Team;
import battlecode.common.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import static zigbotplayer.RobotPlayer.rc;

public class blockChainHandler {

//    public static void main(String[] Args){
//        int[] code = {0x11223344, 0x22334411, 0x33441122, 0x12345678, 0x23456781, 0x34567812, 0};
//        if (isValid(code)){
//            System.out.println("bad1");
//        }
//        signThis(code);
//        if(!isValid(code)){
//            System.out.println("bad2");
//        }
//        System.out.println("done");
//    }

    static int TEAM_HASH = 0x9e82c35a ^ (rc.getTeam() == Team.A ? 0 : 0xFFFFFF);

    public static int checkSum(int[] code){
        CRC32 sum = new CRC32();
        for(int i=0; i<6; i++){
            int item = code[i];
            for(int j=0; j<4; j++){
                sum.update(item);
                item >>= 8;
            }
        }
        return TEAM_HASH ^ (int) sum.getValue();
    }

    public static void signThis(int[] code){
        code[6] = checkSum(code);
    }

    public static boolean isValid(int[] code){
        return code[6] == checkSum(code);
    }

    public boolean submitMessage(RobotPlayer.Message m) throws GameActionException {
        int[] message = new int[7];
        System.arraycopy(m.msg, 0, message, 0, 6);
        signThis(message);
        if (rc.canSubmitTransaction(message, m.price)) {
            rc.submitTransaction(message, m.price);
            return true;
        }
        return false;
    }

    public List<int[]> getMostRecentMessages() throws GameActionException{
        List<int[]> result = new ArrayList<>();
        for(int i=rc.getRoundNum()-1; i>=1; i--) {
            Transaction[] transactions = rc.getBlock(i);
            for(Transaction t : transactions){
                if (isValid(t.getMessage())){
                    result.add(t.getMessage());
                }
            }
            if(!result.isEmpty()){
                return result;
            }
        }
        return result;
    }
}
