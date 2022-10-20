package schnorr.reductions;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sound.midi.SysexMessage;

import dlog.I_DLog_Challenger;
import genericGroups.IGroupElement;
import schnorr.I_Schnorr_EUFNMA_Adversary;
import schnorr.Schnorr_PK;
import utils.Pair;
import utils.NumberUtils;

public class Schnorr_EUFNMA_Reduction extends A_Schnorr_EUFNMA_Reduction {

    public IGroupElement x;
    public IGroupElement g;
    public BigInteger p;
    public Map<Pair<String, IGroupElement>, BigInteger> map = new HashMap<Pair<String, IGroupElement>, BigInteger>();
    //Map<String, BigInteger> map = new HashMap<String, BigInteger>();
    public Schnorr_EUFNMA_Reduction(I_Schnorr_EUFNMA_Adversary<IGroupElement, BigInteger> adversary) {
        super(adversary);
        // Do not change this constructor!
    }

    @Override
    public Schnorr_PK<IGroupElement> getChallenge() {
        // Write your Code here!
        // We have g & x 
        Schnorr_PK<IGroupElement> ele = new Schnorr_PK<IGroupElement>(g, x); // x <- g^x
        return ele;
    }

    @Override
    public BigInteger hash(String message, IGroupElement r) {
        // Write your Code here!
        //var pair = new Triple<String, IGroupElement, IGroupElement>(message, r, x);
        Pair<String, IGroupElement> pair = new Pair<String, IGroupElement>(message, r);
        if (map.containsKey(pair)){ 
            BigInteger bi = map.get(pair);
            return bi;
        }
        else {
            Random rnd = new Random();
            BigInteger bi = NumberUtils.getRandomBigInteger(rnd, p);
            while(map.containsValue(bi) && ! map.containsKey(pair)){
                bi = NumberUtils.getRandomBigInteger(rnd, p);
            }
            map.put(pair, bi);
           //System.out.println(bi);
            return bi;
        }
    }

    @Override
    public BigInteger run(I_DLog_Challenger<IGroupElement> challenger) {
        // Write your Code here!
        // reduction act as adversary, from challenger get challenge
        // You can use the Triple class...
        var dlog_challenge = challenger.getChallenge(); // get a dlog challenge
        x = dlog_challenge.x; // public key
        g = dlog_challenge.generator; // generator
        p = g.getGroupOrder(); //prime
        
        adversary.reset(114514); // second
        var a_1 = adversary.run(this); // first adversary 
        var c_1 = a_1.signature.c;
        var s_1 = a_1.signature.s;

        map = new HashMap<Pair<String, IGroupElement>, BigInteger>();

        adversary.reset(114514); // second 
        var a_2 = adversary.run(this);
        var c_2 = a_2.signature.c;
        var s_2 = a_2.signature.s;
        //BigInteger ans = s_2.subtract(s_1).divide(c_2.subtract(c_1));
        BigInteger ans = ((s_2.subtract(s_1)).multiply((c_2.subtract(c_1)).modInverse(p))).mod(p);
        return ans;
    }
}
