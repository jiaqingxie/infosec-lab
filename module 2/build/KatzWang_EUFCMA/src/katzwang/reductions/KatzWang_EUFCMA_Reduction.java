package katzwang.reductions;

import java.math.BigInteger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map;

import ddh.DDH_Challenge;
import ddh.I_DDH_Challenger;
import genericGroups.IGroupElement;
import katzwang.A_KatzWang_EUFCMA_Adversary;
import katzwang.KatzWangPK;
import katzwang.KatzWangSignature;
import katzwang.KatzWangSolution;
import utils.NumberUtils;
import utils.Pair;
import utils.StringUtils;
import utils.Triple;

public class KatzWang_EUFCMA_Reduction extends A_KatzWang_EUFCMA_Reduction {

    public Map<Triple<IGroupElement, IGroupElement, String>, BigInteger> map = new HashMap<Triple<IGroupElement, IGroupElement, String>, BigInteger>();
    public BigInteger p;
    public IGroupElement g;
    public IGroupElement h;
    public Random rnd = new Random();
    public IGroupElement y_1;
    public IGroupElement y_2;

    public KatzWang_EUFCMA_Reduction(A_KatzWang_EUFCMA_Adversary adversary) {
        super(adversary);
        // Do not change this constructor
    }

    @Override
    public Boolean run(I_DDH_Challenger<IGroupElement, BigInteger> challenger) {
        // Implement your code here!

        // You can use all classes and methods from the util package:
        var ddh_challenge = challenger.getChallenge();
        p = ddh_challenge.generator.getGroupOrder(); // prime
        g = ddh_challenge.generator;
        h = ddh_challenge.y;
        y_1 = ddh_challenge.x;
        y_2 = ddh_challenge.z;
        
        var sol = adversary.run(this);
        if (sol == null){
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public KatzWangPK<IGroupElement> getChallenge() {
        // Implement your code here!
        KatzWangPK<IGroupElement> ele = new KatzWangPK<IGroupElement>(g, h, y_1, y_2);
        return ele;
    }

    @Override
    public BigInteger hash(IGroupElement comm1, IGroupElement comm2, String message) {
        // Implement your code here!
        Triple<IGroupElement, IGroupElement, String> triple = new Triple<IGroupElement, IGroupElement, String>(comm1, comm2, message);
        if (map.containsKey(triple)){ 
            BigInteger bi = map.get(triple);
            return bi;
        }
        else {
            Random rnd = new Random();
            BigInteger bi = NumberUtils.getRandomBigInteger(rnd, p);
            while(map.containsValue(bi) && !map.containsKey(triple)){
                bi = NumberUtils.getRandomBigInteger(rnd, p);
            }
            map.put(triple, bi);
           //System.out.println(bi); 
            return bi;
        }
    }

    @Override
    public KatzWangSignature<BigInteger> sign(String message) {
        // Implement your code here!
        BigInteger c = NumberUtils.getRandomBigInteger(rnd, p);
        BigInteger s = NumberUtils.getRandomBigInteger(rnd, p);
        IGroupElement R_1 = g.power(s).multiply(y_1.power(c.negate()));
        IGroupElement R_2 = h.power(s).multiply(y_2.power(c.negate()));
        //Pair<String, IGroupElement> pair = new Pair<String, IGroupElement>(message, R);
        Triple<IGroupElement, IGroupElement, String> triple = new Triple<IGroupElement, IGroupElement, String>(R_1, R_2, message);
        
        map.put(triple, c);
       
       
        KatzWangSignature<BigInteger> sig = new KatzWangSignature<BigInteger>(c, s);
        return sig;
    }
}
