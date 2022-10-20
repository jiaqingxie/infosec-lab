package katzwang.reductions;

import java.math.BigInteger;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import ddh.I_DDH_Challenger;
import genericGroups.IGroupElement;
import katzwang.A_KatzWang_EUFNMA_Adversary;
import katzwang.KatzWangPK;
import utils.NumberUtils;
import utils.Triple;

public class KatzWang_EUFNMA_Reduction extends A_KatzWang_EUFNMA_Reduction {


    public Map<Triple<IGroupElement, IGroupElement, String>, BigInteger> map = new HashMap<Triple<IGroupElement, IGroupElement, String>, BigInteger>();
    public BigInteger p;
    public IGroupElement g;
    public IGroupElement h;
    public IGroupElement y_1;
    public IGroupElement y_2;

    public KatzWang_EUFNMA_Reduction(A_KatzWang_EUFNMA_Adversary adversary) {
        super(adversary);
        // Do not change this constructor!
    }

    @Override
    public Boolean run(I_DDH_Challenger<IGroupElement, BigInteger> challenger) {
        // Write your Code here!
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
        // Write your Code here!
        KatzWangPK<IGroupElement> ele = new KatzWangPK<IGroupElement>(g, h, y_1, y_2);
        return ele;
    }

    @Override
    public BigInteger hash(IGroupElement comm1, IGroupElement comm2, String message) {
        // Write your Code here!
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

}
