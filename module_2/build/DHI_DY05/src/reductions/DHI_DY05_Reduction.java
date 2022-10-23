package reductions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.text.AsyncBoxView.ChildLocator;

import algebra.SimplePolynomial;
import dhi.DHI_Challenge;
import dhi.I_DHI_Challenger;
import dy05.DY05_PK;
import dy05.I_Selective_DY05_Adversary;
import genericGroups.IGroupElement;
import randomness.Polynomial;
import utils.NumberUtils;
import utils.Pair;
import utils.StringUtils;
import utils.Triple;

public class DHI_DY05_Reduction implements I_DHI_DY05_Reduction {
    // Do not remove this field!
    private final I_Selective_DY05_Adversary adversary;
    private DHI_Challenge challenge;
    private IGroupElement generator;
    private BigInteger order;
    private int apreimage;
    private SimplePolynomial fz;
    private ArrayList<IGroupElement> g_beta = new ArrayList<IGroupElement>();


    public DHI_DY05_Reduction(I_Selective_DY05_Adversary adversary) {
        // Do not change this constructor!
        this.adversary = adversary;
    }

    @Override
    public IGroupElement run(I_DHI_Challenger challenger) {
        // Write Code here!

        challenge = challenger.getChallenge();
        generator = challenge.get(0); //g ;
        g_beta.add(generator);
        order = generator.getGroupOrder();
        fz = new SimplePolynomial(order, 1);

        IGroupElement gamma_star = adversary.run(this); // sigma*
        SimplePolynomial f_0_z = fz.div(new SimplePolynomial(order, apreimage, 1)); // save gamma_0 to gamma_q_2
        SimplePolynomial rem = fz.subtract(f_0_z.multiply(new SimplePolynomial(order, apreimage, 1)));
        BigInteger gamma_1 = rem.get(0); // gamma-1

        IGroupElement ans = generator.power(BigInteger.valueOf(0));
        for (int i =0; i < challenge.size() -2; i++){
            ans = ans.multiply(g_beta.get(i).power(f_0_z.get(i).negate()));
        }
        ans = (ans.multiply(gamma_star)).power(gamma_1.modInverse(order));
        return ans;
    }

    @Override
    public void receiveChallengePreimage(int _challenge_preimage) throws Exception {
        this.apreimage = _challenge_preimage; // x_0
        // Write Code here!
    }

    @Override
    public IGroupElement eval(int preimage) {
        // Write Code here!
        SimplePolynomial f_i_z = fz.div(new SimplePolynomial(order, preimage, 1));
        IGroupElement ans = generator.power(BigInteger.valueOf(0));
        for (int i = 0; i < challenge.size() - 2; i++){
            ans = ans.multiply(g_beta.get(i).power(f_i_z.get(i)));
        }
        return ans;
    }

    @Override
    public DY05_PK getPK() {
        for (int i = 0; i < challenge.size()-1; i++){
            if (i != apreimage){
                fz = fz.multiply(new SimplePolynomial(order, i, 1)); //fz
            }
        }
        for (int i = 1; i < challenge.size(); i++){
            SimplePolynomial x0_p = new SimplePolynomial(order, 1);
            for (int j = 0; j < i; j++){
                x0_p = x0_p.multiply(new SimplePolynomial(order, -apreimage, 1)); // beta^k = (alpha - x0)^k
                // here it should return the polynomial like this
                // x0_p = (-x0)^k + Ck1(-x0)^k-1 x * alpha + ... + alpha^k
            }
            IGroupElement g_beta_i = generator.power(BigInteger.valueOf(0));
            for (int k = 0; k <= i; k++){
                IGroupElement tmp_ans = challenge.get(k).power(x0_p.get(k));
                g_beta_i = g_beta_i.multiply(tmp_ans);
            }
            g_beta.add(g_beta_i); // begin with i = 1
        }

        IGroupElement ans = generator.power(BigInteger.valueOf(0));
        IGroupElement h = generator.power(BigInteger.valueOf(0));

        for (int i = 1 ; i < challenge.size(); i++){
            ans = ans.multiply(g_beta.get(i).power(fz.get(i-1)));
        }
        
        for (int i = 0; i < challenge.size()-1; i++){
            h = h.multiply(g_beta.get(i).power(fz.get(i)));
        }
        DY05_PK PK = new DY05_PK(h, ans);
        return PK;
        }
}