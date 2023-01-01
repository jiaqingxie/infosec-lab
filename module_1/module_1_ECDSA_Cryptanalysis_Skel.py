import math
import random
from fpylll import LLL
from fpylll import BKZ
from fpylll import IntegerMatrix
from fpylll import CVP
from fpylll import SVP
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

def egcd(a, b):
    # Implement the Euclidean algorithm for gcd computation
    if a == 0:
        return b, 0, 1
    else:
        g, y, x = egcd(b % a, a)
        return g, x - (b // a) * y, y

def mod_inv(a, p):
    # Implement a function to compute the inverse of a modulo p
    # Hint: Use the gcd algorithm implemented above
    if a < 0:
        return p - mod_inv(-a, p)
    g, x, y = egcd(a, p)
    if g != 1:
        raise ArithmeticError("Modular inverse does not exist")
    else:
        return x % p

def check_x(x, Q):
    """ Given a guess for the secret key x and a public key Q = [x]P,
        checks if the guess is correct.

        :params x:  secret key, as an int
        :params Q:  public key, as a tuple of two ints (Q_x, Q_y)
    """
    x = int(x)
    if x <= 0:
        return False
    Q_x, Q_y = Q
    sk = ec.derive_private_key(x, ec.SECP256R1())
    pk = sk.public_key()
    xP = pk.public_numbers()
    return xP.x == Q_x and xP.y == Q_y

def recover_x_known_nonce(k, h, r, s, q):
    # Implement the "known nonce" cryptanalytic attack on ECDSA
    # The function is given the nonce k, (h, r, s) and the base point order q
    # The function should compute and return the secret signing key x
    # xr mod q = k * s - h mod q ====> x mod q = r^-1 * (k * s - h) mod q
    x = (mod_inv(r, q) * (k * s - h)) % q # 0 <= x <= q
    return x

def recover_x_repeated_nonce(h_1, r_1, s_1, h_2, r_2, s_2, q):
    # Implement the "repeated nonces" cryptanalytic attack on ECDSA
    # The function is given the (hashed-message, signature) pairs (h_1, r_1, s_1) and (h_2, r_2, s_2) generated using the same nonce
    # The function should compute and return the secret signing key x
    # xr1 – ks1 = h1 mod q,  xr2 – ks2 = h2 mod q 
    # ====>  x = (h1s2 – h2s1) ⋅ (r2s1 – r1s2)^-1 mod q 
    x = (h_1 * s_2 - h_2 * s_1) * mod_inv(r_2 * s_1 - r_1 * s_2, q) % q # 0 <= x <= q
    return x

def MSB_to_Padded_Int(N, L, list_k_MSB):
    # Implement a function that does the following: 
    # Let a is the integer represented by the L most significant bits of the nonce k 
    # The function should return a.2^{N - L} + 2^{N -L -1}
    s = ''.join(str(x) for x in list_k_MSB)
    a =  int(s, 2)
    return a * 2**(N - L) + 2**(N - L - 1) # a.2^{N - L} + 2^{N - L -1}

def LSB_to_Int(list_k_LSB):
    # Implement a function that does the following: 
    # Let a is the integer represented by the L least significant bits of the nonce k 
    # The function should return a
    s = ''.join(str(x) for x in list_k_LSB)
    a =  int(s, 2)
    return a

def setup_hnp_single_sample(N, L, list_k_MSB, h, r, s, q, givenbits="msbs", algorithm="ecdsa"):
    #print(q)
    # Implement a function that sets up a single instance for the hidden number problem (HNP)
    # The function is given a list of the L most significant bts of the N-bit nonce k, along with (h, r, s) and the base point order q
    # The function should return (t, u) computed as described in the lectures
    # In the case of EC-Schnorr, r may be set to h
    # analysis:
    # if ecsda: then rs^-1x mod q= (k-hs^-1) mod q then in this mode t = rs^-1, z = hs^-1, u = (k - z) mod q
    # however ecschnorr: then hx mod q = (k - s) mod q, if we want to keep the same form as ecsda, then r = h, z = s
    if algorithm == "ecdsa":
        t = r * mod_inv(s, q) % q
        if givenbits == "msbs":
            u = (MSB_to_Padded_Int(N, L, list_k_MSB) - h * mod_inv(s, q)) % q
        elif givenbits == "lsbs":
            u = (LSB_to_Int(list_k_MSB) -  h * mod_inv(s, q)) % q
        else:
            raise ValueError("Wrong givenbits value")
    elif algorithm == "ecschnorr":
        t = h
        if givenbits == "msbs":
            u = (MSB_to_Padded_Int(N, L, list_k_MSB) - s) % q
        elif givenbits == "lsbs":
            u = (LSB_to_Int(list_k_MSB) - s) % q
        else:
            raise ValueError("Wrong givenbits value")
    else:
        raise ValueError("Wrong algorithm value")
    if givenbits == "lsbs":
        t = t * mod_inv(2**L, q) % q
        u = u * mod_inv(2**L, q) % q
    return (t, u)

def setup_hnp_all_samples(N, L, num_Samples, listoflists_k_MSB, list_h, list_r, list_s, q, givenbits="msbs", algorithm="ecdsa"):
    # Implement a function that sets up n = num_Samples many instances for the hidden number problem (HNP)
    # For each instance, the function is given a list the L most significant bits of the N-bit nonce k, along with (h, r, s) and the base point order q
    # The function should return a list of t values and a list of u values computed as described in the lectures
    # Hint: Use the function you implemented above to set up the t and u values for each instance
    # In the case of EC-Schnorr, list_r may be set to list_h
    t_, u_ = [],[]
    if algorithm == "ecschnorr": list_r = list_h
    for i in range(len(listoflists_k_MSB)):
        t, u = setup_hnp_single_sample(N, L, listoflists_k_MSB[i], list_h[i], list_r[i], list_s[i], q, givenbits, algorithm)
        t_.append(t)
        u_.append(u)
    return t_, u_

def hnp_to_cvp(N, L, num_Samples, list_t, list_u, q):
    # Implement a function that takes as input an instance of HNP and converts it into an instance of the closest vector problem (CVP)
    # The function is given as input a list of t values, a list of u values and the base point order q
    # The function should return the CVP basis matrix B (to be implemented as a nested list) and the CVP target vector u (to be implemented as a list)
    # NOTE: The basis matrix B and the CVP target vector u should be scaled appropriately. Refer lecture slides and lab sheet for more details 
    cvp_B = IntegerMatrix(num_Samples + 1, num_Samples + 1) # a matrix where all elements are zero
    for i in range(num_Samples + 1):
        cvp_B[i, i] = q * (2**(L + 1)) # scaled
        cvp_B[num_Samples, i] = list_t[i] * (2**(L + 1)) if i <= num_Samples - 1 else 1 # scaled to be an integer
    cvp_B = IntegerMatrix.from_matrix(list(cvp_B)) # CVP basis matrix B (from list)
    for i in range(num_Samples): 
        list_u[i] = list_u[i] * (2**(L + 1))
    cvp_u = list(list_u) + [0]
    return (cvp_B, cvp_u) 
    
def cvp_to_svp(N, L, num_Samples, cvp_basis_B, cvp_list_u):
    # Implement a function that takes as input an instance of CVP and converts it into an instance of the shortest vector problem (SVP)
    # Your function should use the Kannan embedding technique in the lecture slides
    # The function is given as input a CVP basis matrix B and the CVP target vector u
    # The function should use the Kannan embedding technique to output the corresponding SVP basis matrix B' of apropriate dimensions.
    # The SVP basis matrix B' should again be implemented as a nested list
    # M = k* lambda / 2, where k is a constant? ? lambda / 2 is the upper bound
    #print(cvp_basis_B[0,0])
    det_ = cvp_basis_B[0, 0]**(num_Samples/(num_Samples + 1)) # det(L)^(1/n)
    tmp_= ((num_Samples + 1)/(2 * math.pi * math.e))**(1/2) # sqrt(n / 2 * pi * e)
    lambd_1 =  det_ * tmp_ # lambd_1
    k = 0.01
    M = int(k * lambd_1/2) 
    svp_ = IntegerMatrix(cvp_basis_B)
    svp_.resize(cvp_basis_B.nrows + 1, cvp_basis_B.ncols + 1)
    for i in range(cvp_basis_B.ncols + 1):
        svp_[num_Samples + 1, i] = cvp_list_u[i] if i != cvp_basis_B.ncols else M 
    return svp_

def solve_cvp(cvp_basis_B, cvp_list_u):
    # Implement a function that takes as input an instance of CVP and solves it using in-built CVP-solver functions from the fpylll library
    # The function is given as input a CVP basis matrix B and the CVP target vector u
    # The function should output the solution vector v (to be implemented as a list)
    # NOTE: The basis matrix B should be processed appropriately before being passes to the fpylll CVP-solver. See lab sheet for more details
    _ = LLL.reduction(cvp_basis_B)
    v = list(CVP.closest_vector(cvp_basis_B, cvp_list_u, method = "fast"))
    return v

def solve_svp(svp_basis_B):
    # Implement a function that takes as input an instance of SVP and solves it using in-built SVP-solver functions from the fpylll library
    # The function is given as input the SVP basis matrix B
    # The function should output a list of candidate vectors that may contain x as a coefficient
    # NOTE: Recall from the lecture and also from the exercise session that for ECDSA cryptanalysis based on partial nonces, you might want
    #       your function to include in the list of candidate vectors the *second* shortest vector (or even a later one). 
    # If required, figure out how to get the in-built SVP-solver functions from the fpylll library to return the second (or later) shortest vector
    _ = LLL.reduction(svp_basis_B)
    # the fact is after reduction, svp_basis_B becomes a sorted lattice by the distance
    return list(svp_basis_B)

def recover_x_partial_nonce_CVP(Q, N, L, num_Samples, listoflists_k_MSB, list_h, list_r, list_s, q, givenbits="msbs", algorithm="ecdsa"):
    # Implement the "repeated nonces" cryptanalytic attack on ECDSA and EC-Schnorr using the in-built CVP-solver functions from the fpylll library
    # The function is partially implemented for you. Note that it invokes some of the functions that you have already implemented
    list_t, list_u = setup_hnp_all_samples(N, L, num_Samples, listoflists_k_MSB, list_h, list_r, list_s, q, givenbits, algorithm)
    cvp_basis_B, cvp_list_u = hnp_to_cvp(N, L, num_Samples, list_t, list_u, q)
    v_List = solve_cvp(cvp_basis_B, cvp_list_u)
    # The function should recover the secret signing key x from the output of the CVP solver and return it
    # solution x: [a1, a2, a3, ...., x] last element
    x = v_List[-1] % q # last element idx
    #assert check_x(x, Q) == True
    return x

def recover_x_partial_nonce_SVP(Q, N, L, num_Samples, listoflists_k_MSB, list_h, list_r, list_s, q, givenbits="msbs", algorithm="ecdsa"):
    # Implement the "repeated nonces" cryptanalytic attack on ECDSA and EC-Schnorr using the in-built CVP-solver functions from the fpylll library
    # The function is partially implemented for you. Note that it invokes some of the functions that you have already implemented
    list_t, list_u = setup_hnp_all_samples(N, L, num_Samples, listoflists_k_MSB, list_h, list_r, list_s, q, givenbits, algorithm)
    cvp_basis_B, cvp_list_u = hnp_to_cvp(N, L, num_Samples, list_t, list_u, q)
    svp_basis_B = cvp_to_svp(N, L, num_Samples, cvp_basis_B, cvp_list_u)
    list_of_f_List = solve_svp(svp_basis_B)
    # The function should recover the secret signing key x from the output of the SVP solver and return it
    for i in range(len(list_of_f_List)):
        shortest = list_of_f_List[i] # in fact, high prop for the second longest
        x = (cvp_list_u[-1] - shortest[-2]) % q # v = u_cvp - f, the second last since f' = [f, M]
        if check_x(x, Q) == True:
            return x
    return x

# testing code: do not modify

from module_1_ECDSA_Cryptanalysis_tests import run_tests

run_tests(recover_x_known_nonce,
    recover_x_repeated_nonce,
    recover_x_partial_nonce_CVP,
    recover_x_partial_nonce_SVP
)