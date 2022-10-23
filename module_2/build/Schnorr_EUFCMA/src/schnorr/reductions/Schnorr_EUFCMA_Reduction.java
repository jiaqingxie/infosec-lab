package schnorr.reductions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import javax.sound.midi.SysexMessage;

import basics.IChallenger;

import java.util.Map;

import dlog.DLog_Challenge;
import dlog.I_DLog_Challenger;
import genericGroups.IGroupElement;
import schnorr.I_Schnorr_EUFCMA_Adversary;
import schnorr.SchnorrSignature;
import schnorr.SchnorrSolution;
import schnorr.Schnorr_PK;
import utils.NumberUtils;
import utils.Pair;

public class Schnorr_EUFCMA_Reduction extends A_Schnorr_EUFCMA_Reduction {

    public IGroupElement x;
    public IGroupElement g;
    public BigInteger p;
    public Random rnd = new Random();
    public Map<Pair<String, IGroupElement>, BigInteger> map = new HashMap<Pair<String, IGroupElement>, BigInteger>();
    public Schnorr_EUFCMA_Reduction(I_Schnorr_EUFCMA_Adversary<IGroupElement, BigInteger> adversary) {
        super(adversary);
        // Do not change this constructor!
    }

    @Override
    public Schnorr_PK<IGroupElement> getChallenge() {
        // Implement your code here!
        Schnorr_PK<IGroupElement> ele = new Schnorr_PK<IGroupElement>(g, x); // x <- g^x
        return ele;
    }

    @Override
    public SchnorrSignature<BigInteger> sign(String message) {
        // Implement your code here!
        
        BigInteger c = NumberUtils.getRandomBigInteger(rnd, p);
        BigInteger s = NumberUtils.getRandomBigInteger(rnd, p);
        IGroupElement R = g.power(s).multiply(x.power(c.negate()));
        Pair<String, IGroupElement> pair = new Pair<String, IGroupElement>(message, R);
        if (!map.containsKey(pair)){
            map.put(pair, c);
        }
       
        SchnorrSignature<BigInteger> sig = new SchnorrSignature<BigInteger>(c, s);
        return sig;
    }

    @Override
    public BigInteger hash(String message, IGroupElement r) {
        // Implement your code here!
        Pair<String, IGroupElement> pair = new Pair<String, IGroupElement>(message, r);
        if (map.containsKey(pair)){ 
            BigInteger bi = map.get(pair);
            return bi;
        }
        else {
            //Random rnd = new Random();
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
        // Implement your code here!
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
