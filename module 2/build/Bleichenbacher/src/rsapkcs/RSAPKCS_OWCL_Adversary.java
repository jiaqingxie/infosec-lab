package rsapkcs;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import utils.NumberUtils;
import utils.Pair;
import static utils.NumberUtils.ceilDivide;

public class RSAPKCS_OWCL_Adversary implements I_RSAPKCS_OWCL_Adversary {

    public I_RSAPKCS_OWCL_Challenger challenge;
    public BigInteger ciphertext;
    public RSAPKCS_PK PK_e_N;
    public BigInteger e;
    public BigInteger N;
    public Random rnd = new Random();
    public ArrayList<Pair<BigInteger, BigInteger>> M = new ArrayList<Pair<BigInteger, BigInteger>>();

    public RSAPKCS_OWCL_Adversary() {
        // Do not change this constructor!
    }

    // correct
    public ArrayList<Pair<BigInteger, BigInteger>> intersect(ArrayList<Pair<BigInteger, BigInteger>> _M, BigInteger a, BigInteger b){
        int counter = 0;
        for (Pair<BigInteger, BigInteger> pair: _M){
            if (pair.first.compareTo(b) != 1 && a.compareTo(pair.second) != 1){
                var a_ = a.min(pair.first);
                var b_ = b.max(pair.second);
                _M.set(counter, new Pair<BigInteger, BigInteger>(a_, b_));
               // return _M;
               return _M;
            }
            counter+=1;
        }
        _M.add(new Pair<BigInteger,BigInteger>(a,b));
        return _M;
    }

    //correct 
    public Pair<BigInteger, BigInteger> step_1(BigInteger c, BigInteger N, BigInteger e) throws Exception{
        if (challenge.isPKCSConforming(c)){
            return new Pair<BigInteger,BigInteger>(BigInteger.valueOf(1), c);
        }

        BigInteger _s0 = NumberUtils.getRandomBigInteger(rnd, N);
        BigInteger _c0 = (c.multiply(_s0.modPow(e, N))).mod(N);
        while (!challenge.isPKCSConforming(_c0)){
            _s0 = NumberUtils.getRandomBigInteger(rnd, N);
            _c0 = (c.multiply(_s0.modPow(e, N))).mod(N);
        }
        Pair<BigInteger, BigInteger> pair_c_s = new Pair<BigInteger,BigInteger>(_s0, _c0);
        return pair_c_s; // <s0, c0>
    }
    
    //correct 
    public BigInteger step_2_a(BigInteger c0, BigInteger N, BigInteger e, BigInteger B) throws Exception{
        BigInteger B_3 = B.multiply(BigInteger.valueOf(3));
        BigInteger _s1 = ceilDivide(N, B_3);
        BigInteger _c1 = (c0.multiply(_s1.modPow(e, N))).mod(N);
        while (!challenge.isPKCSConforming(_c1)){
            _s1 = _s1.add(BigInteger.valueOf(1));
            _c1 = (c0.multiply(_s1.modPow(e, N))).mod(N);
        }
        return _s1;
    }

    //correct
    public BigInteger step_2_b(BigInteger e, BigInteger N, BigInteger c0, BigInteger s) throws Exception{
        s = s.add(BigInteger.valueOf(1));
        BigInteger _c = (c0.multiply(s.modPow(e, N))).mod(N); 
        while (!challenge.isPKCSConforming(_c)){
            s = s.add(BigInteger.valueOf(1));
            _c = (c0.multiply(s.modPow(e, N))).mod(N); 
        }
        return s;
    }

    // correct
    public BigInteger step_2_c(BigInteger e, BigInteger N, BigInteger c0, BigInteger s, BigInteger a, BigInteger b, BigInteger B) throws Exception{
        // s = si_1
        BigInteger b_s = b.multiply(s);
        BigInteger B_2 = B.multiply(BigInteger.valueOf(2));
        BigInteger B_3 = B.multiply(BigInteger.valueOf(3));
        BigInteger r_i = ceilDivide((b_s.subtract(B_2)), N).multiply(BigInteger.valueOf(2));
        while (true){
            BigInteger s_i_left = ceilDivide(B_2.add(r_i.multiply(N)), b); 
            BigInteger s_i_right = (B_3.add(r_i.multiply(N))).divide(a);
            BigInteger s_i = s_i_left;
            BigInteger _c = (c0.multiply(s_i.modPow(e, N))).mod(N);

            while (!challenge.isPKCSConforming(_c) && s_i.compareTo(s_i_right) !=1){
                s_i = s_i.add(BigInteger.valueOf(1));
                _c = (c0.multiply(s_i.modPow(e, N))).mod(N);   
            }

            if(challenge.isPKCSConforming(_c) && s_i.compareTo(s_i_right) !=1){
                return s_i;
            }
            else{
                r_i = r_i.add(BigInteger.valueOf(1));
            }
        }
    }

    // correct
    public ArrayList<Pair<BigInteger, BigInteger>> step_3(BigInteger s, BigInteger B, BigInteger N, ArrayList<Pair<BigInteger, BigInteger>> M){
        ArrayList<Pair<BigInteger, BigInteger>> _M = new ArrayList<Pair<BigInteger, BigInteger>>();
        for (Pair<BigInteger, BigInteger> pair: M){
            BigInteger B_3 = B.multiply(BigInteger.valueOf(3));
            BigInteger B_2 = B.multiply(BigInteger.valueOf(2));
            BigInteger left_max = ceilDivide(((pair.first.multiply(s)).subtract(B_3)).add(BigInteger.valueOf(1)), N);
            BigInteger right_max = ((pair.second.multiply(s)).subtract(B_2)).divide(N);
            BigInteger r_i = left_max;
            while (r_i.compareTo(right_max) != 1){
                BigInteger B_2_rn = B_2.add(N.multiply(r_i));
                BigInteger B_3_rn = B_3.add(N.multiply(r_i));
                BigInteger _a_max = pair.first.max(ceilDivide(B_2_rn, s));
                BigInteger _b_max = pair.second.min((B_3_rn.subtract(BigInteger.valueOf(1))).divide(s));
                _M = intersect(_M, _a_max, _b_max);
                r_i = r_i.add(BigInteger.valueOf(1));
            }
        }
        return _M;
    }

    // correct
    public BigInteger BleiAttack(BigInteger e, BigInteger c, BigInteger N) throws Exception{
        BigInteger k = ceilDivide(BigInteger.valueOf(N.bitLength()), BigInteger.valueOf(8)); // k
        BigInteger k_2_8 = (k.subtract(BigInteger.valueOf(2))).multiply(BigInteger.valueOf(8));
        BigInteger B = BigInteger.valueOf(2).modPow(k_2_8, N); // B
        Pair<BigInteger, BigInteger> pair_s0_c0 = step_1(c, N, e); // pair<s0, c0>
        BigInteger B_2 = B.multiply(BigInteger.valueOf(2)); // 2* B
        BigInteger B_3_1 = (B.multiply(BigInteger.valueOf(3))).subtract(BigInteger.valueOf(1));
        M.add(new Pair<BigInteger,BigInteger>(B_2, B_3_1)); // M = [(2B, 3B - 1)]
        BigInteger s = step_2_a(pair_s0_c0.second, N, e, B);

        M = step_3(s, B, N, M);
        
        while (true){
            if (M.size() > 1){
                s = step_2_b(e, N, pair_s0_c0.second, s);
            }
            else{
                Pair<BigInteger, BigInteger> pair = M.get(0);
                if (pair.first.equals(pair.second)){
                    var m = pair.first.multiply(pair_s0_c0.first.modPow(BigInteger.valueOf(-1), N)).mod(N);
                    return m;
                }
                s = step_2_c(e, N, pair_s0_c0.second, s, pair.first, pair.second, B);
            }
            M = step_3(s, B, N, M);
        }
    }

    public BigInteger m_pad_to_m(BigInteger m){
        String s = m.toString(16);
        int where_true_m = 0;
        for (int i = 0; i< s.length()-1; i++){
            if (s.charAt(i) == '0' && s.charAt(i+1) == '0' && i > 15){
                where_true_m = i+2;
                break;
            }
        }
        String true_m = s.substring(where_true_m, s.length());
        BigInteger ans = new BigInteger(true_m, 16);
        return ans;
    }

    @Override
    public BigInteger run(final I_RSAPKCS_OWCL_Challenger challenger) {
        // Write code here!
        challenge = challenger;
        ciphertext = challenge.getChallenge();
        PK_e_N = challenge.getPk();
        e = PK_e_N.exponent;
        N = PK_e_N.N;
        BigInteger m = BigInteger.valueOf(0);
        try {
            m = BleiAttack(e, ciphertext, N); // be care that we need to change it to the byte(hex) string to take the real m !!!!
            //System.out.println(m.toString(16));
            m = m_pad_to_m(m);
            //System.out.println(m.toString(16));
            
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }    
        return m;
    }
}