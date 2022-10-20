package cdh_quadratic;

import java.math.BigInteger;
import java.util.Random;

import cdh.CDH_Challenge;
import cdh.I_CDH_Challenger;
import genericGroups.IGroupElement;
import utils.NumberUtils;
import utils.Pair;
import utils.StringUtils;
import utils.Triple;

/**
 * This is the file you need to implement.
 * 
 * Implement the methods {@code run} and {@code getChallenge} of this class.
 * Do not change the constructor of this class.
 */
public class CDH_Quad_Reduction extends A_CDH_Quad_Reduction<IGroupElement> {


    public IGroupElement x;
    public IGroupElement y;
    public IGroupElement g;
    public IGroupElement sol; // solution given by the adversary 
    public CDH_Challenge<IGroupElement> challenge;
    public BigInteger p; // group order (big prime)
    /**
     * Do NOT change or remove this constructor. When your reduction can not provide
     * a working standard constructor, the TestRunner will not be able to test your
     * code and you will get zero points.
     */
    public CDH_Quad_Reduction() {
        // Do not add any code here!
    }

    public IGroupElement f1(IGroupElement g, IGroupElement gX, IGroupElement gY) {
        // This function will always return g^(axy + bx + cy + d)
        this.x = gX;
        this.y = gY;
        this.g = g;
        challenge = getChallenge();
        IGroupElement ff = adversary.run(this);
        return ff;
    }
    
    public IGroupElement f2(IGroupElement g, IGroupElement gX, IGroupElement gY) {
        // This function will always return g^(axy + bx + cy)
        IGroupElement all = f1(g, gX, gY);
        IGroupElement d = f1(g, gX.power(BigInteger.valueOf(0)), gY.power(BigInteger.valueOf(0))); //gd
        IGroupElement ans = all.multiply(d.power(BigInteger.valueOf(-1)));
        return ans;
    }
    
    public IGroupElement f3(IGroupElement g, IGroupElement gX, IGroupElement gY) {
        // This function will always return g^(axy + bx)
        IGroupElement all_nod = f2(g, gX, gY);
        IGroupElement c = f2(g, gX.power(BigInteger.valueOf(0)), gY); //gcy
        IGroupElement ans = all_nod.multiply(c.power(BigInteger.valueOf(-1)));
        return ans;
    }
    
    public IGroupElement f4(IGroupElement g, IGroupElement gX, IGroupElement gY) {
        // This function will always return g^(axy)
        IGroupElement all_nod_noc = f3(g, gX, gY);  // g^axy * gbx
        IGroupElement b = f2(g, gX, gY.power(BigInteger.valueOf(0))); //gbx
        IGroupElement ans = all_nod_noc.multiply(b.power(BigInteger.valueOf(-1)));
        return ans;
    }

    @Override
    public IGroupElement run(I_CDH_Challenger<IGroupElement> challenger) {
        // This is one of the both methods you need to implement.

        // By the following call you will receive a DLog challenge.
        CDH_Challenge<IGroupElement> challenge = challenger.getChallenge();
        x = challenge.x;
        y = challenge.y;
        g = challenge.generator;
        p = challenge.generator.getGroupOrder();


        // your reduction does not need to be tight. I.e., you may call
        // adversary.run(this) multiple times.

        // Remember that this is a group of prime order p.
        // In particular, we have a^(p-1) = 1 mod p for each a != 0.


        // my solution:
        // ----------------------------------
        // Note that f4(g^a, g^axy, g^a^-2) returns g^xy which is the answer
        // we have g^axy, g^a, now we shoud compute g^(a^-2)
        // Which is equivalent to computing g^(a^p-3) since 
        // g^(a^p-3) = g^(a^-2) mod p according to Fermat's little theorem
        // We use f4 function to perform "add and double" operation to get p-3
        // Since we need to run it in O(logP) time 
        var g_axy = f4(g, x, y);
        var g_a = f4(g, g, g); // since f4(g, gx, gy) returns g^axy, here x = y = 1
        Boolean meet_first_nonzero = false;
        Boolean current = false;
        Boolean first = true;
        IGroupElement small = g;

        IGroupElement big = f4(g, g, g); // g^a
        BigInteger p_3 = p.subtract(BigInteger.valueOf(3));
        for(int i = p_3.bitLength(); i >= 0; i--){
            current = p_3.testBit(i);
            if (current){ //current == 1
                if (!meet_first_nonzero){
                    meet_first_nonzero = true;
                }
                else{
                    if (first){
                        big = f4(g, g_a, g_a);
                        small = f4(g, g_a, g);  
                        first = false;      
                        continue;
                    }
                    small = f4(g, big, small); 
                    big = f4(g, big, big);
                }
            }
            else{
                if (!meet_first_nonzero){
                    continue;
                }
                else{
                    if (first){
                        big = f4(g, g_a, g);
                        small = g_a;  
                        first = false;     
                        continue; 
                    }
                    big = f4(g, big, small);
                    small = f4(g, small, small);
                }
            }
        }
        return f4(g, g_axy, big);
    }

    @Override
    public CDH_Challenge<IGroupElement> getChallenge() {

        // This is the second method you need to implement.
        // You need to create a CDH challenge here which will be given to your CDH
        // adversary.
        // Instead of null, your cdh challenge should consist of meaningful group
        // elements.
        CDH_Challenge<IGroupElement> cdh_challenge = new CDH_Challenge<IGroupElement>(g, x, y);
        return cdh_challenge;
    }
}
