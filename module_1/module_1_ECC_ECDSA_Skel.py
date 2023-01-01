from logging import raiseExceptions
import math
import random
import warnings
import hashlib

# Euclidean algorithm for gcd computation
def egcd(a, b):
    if a == 0:
        return b, 0, 1
    else:
        g, y, x = egcd(b % a, a)
        return g, x - (b // a) * y, y

# Modular inversion computation
def mod_inv(a, p):
    if a < 0:
        return p - mod_inv(-a, p)
    g, x, y = egcd(a, p)
    if g != 1:
        raise ArithmeticError("Modular inverse does not exist")
    else:
        return x % p

# Function to map a message to a bit string
def hash_message_to_bits(msg):
    h = hashlib.sha256()
    h.update(msg.encode())
    h_as_bits = ''.join(format(byte, '08b') for byte in h.digest())
    return h_as_bits

# Function to map a truncated bit string to an integer modulo q
def bits_to_int(h_as_bits, q):
    val = 0
    len = int(math.log(q, 2) + 1)
    for i in range(len):
        val = val * 2
        if(h_as_bits[i] == '1'):
            val = val + 1
    return val % q

# An elliptic curve is represented as an object of type Curve. 
# Note that for this lab, we use the short Weierstrass form of representation.
class Curve(object):

    def __init__(self, a, b, p, P_x, P_y, q):
        self.a = a
        self.b = b
        self.p = p
        self.P_x = P_x
        self.P_y = P_y
        self.q = q

    def is_singular(self):
        return (4 * self.a**3 + 27 * self.b**2) % self.p == 0

    def on_curve(self, x, y):
        return (y**2 - x**3 - self.a * x - self.b) % self.p == 0

    def is_equal(self, other):
        if not isinstance(other, Curve):
            return False
        return self.a == other.a and self.b == other.b and self.p == other.p

# A point at infinity on an elliptic curve is represented separately as an object of type PointInf. 
# We make this distinction between a point at infinity and a regular point purely for the ease of implementation.
class PointInf(object):

    def __init__(self, curve):
        self.curve = curve

    def is_equal(self, other):
        if not isinstance(other, PointInf):
            return False
        return self.curve.is_equal(other.curve)

    def negate(self):
        # Write a function that negates a PointInf object.        
        # Ths is an optional extension and is not evaluated
        # - O = O
        return self

    def double(self):
        # Write a function that doubles a PointInf object.
        # O + O = O
        return self 

    def add(self, other):
        # Write a function that adds a Point object (or a PointInf object) to a PointInf object. 
        # See below for the description of a Point object
        # Make sure to output the correct kind of object depending on whether "other" is a Point object or a PointInf object 
        return other if isinstance(other, Point) else self 

# A point on an elliptic curve is represented as an object of type Point. 
# Note that for this lab, we will use the affine coordinates-based representation of a point on an elliptic curve.
class Point(object):

    def __init__(self, curve, x, y):
        self.curve = curve
        self.x = x
        self.y = y
        self.p = self.curve.p
        self.on_curve = True
        if not self.curve.on_curve(self.x, self.y):
            warnings.warn("Point (%d, %d) is not on curve \"%s\"" % (self.x, self.y, self.curve))
            self.on_curve = False

    def is_equal(self, other):
        if not isinstance(other, Point):
            return False
        return self.curve.is_equal(other.curve) and self.x == other.x and self.y == other.y

    def negate(self):
        # Write a function that negates a Point object and returns the resulting Point object
        # Ths is an optional extension and is not evaluated
        return Point(self.curve, self.x, - self.y % self.p) # P = (x, y) ===> -P = (x, -y)

    def double(self):
        # Write a function that doubles a Point object and returns the resulting Point object
        # ppt page 13
        lambd = (3 * self.x**2 + self.curve.a) * mod_inv(2 * self.y, self.p) % self.p
        x_ = lambd**2 - 2 * self.x
        y_ = lambd * (self.x - x_) - self.y
        return Point(self.curve, x_ % self.p, y_ % self.p)

    def add(self, other):
        # Write a function that adds a Point object (or a PointInf object) to the current Point object and returns the resulting Point object
        # 1. PointInf + Point
        if isinstance(other, PointInf): return self # O + P = P
        elif isinstance(other, Point):
            if self.is_equal(other.negate()): return PointInf(self.curve) #P + (-P) = O
            elif self.is_equal(other):return self.double() # P + P = [2]P
            else: # x2: other.x, x1: self.x
                lambd = (other.y - self.y) * mod_inv((other.x - self.x), self.p) % self.p
                x_ = lambd**2 - self.x - other.x
                y_ = lambd * (self.x - x_) - self.y
                return Point(self.curve, x_ % self.p, y_ % self.p)

    def scalar_multiply(self, scalar):
        # Write a function that performs a scalar multiplication on the current Point object and returns the resulting Point object 
        # Make sure to check that the scalar is of type int or long
        # Your function need not be "constant-time"
        if not isinstance(scalar, int): raise TypeError("the scalar is not int type") # if not int, then raise error
        else:
            ans = PointInf(self.curve) 
            for bit in f'{scalar:b}': # double & add
                ans = ans.double()  
                ans = ans.add(self) if int(bit) == 1 else ans 
            return ans

    def scalar_multiply_Montgomery_Ladder(self, scalar):
        # Write a function that performs a "constant-time" scalar multiplication on the current Point object and returns the resulting Point object 
        # Make sure to check that the scalar is of type int or long
        # Implement an elementary timer to check that your implementation is indeed constant-time
        # This is not graded but is an extension for your to try out on your own
        # Montgomery_Ladder algorithm
        R_0 = PointInf(self.curve)
        R_1 = self
        for bit in f'{scalar:b}':
            if int(bit) == 0:
                R_1 = R_0.add(R_1)
                R_0 = R_0.double()
            else:
                R_0 = R_0.add(R_1)
                R_1 = R_1.double()
        return R_0

# The parameters for an ECDSA scheme are represented as an object of type ECDSA_Params
class ECDSA_Params(object):
    def __init__(self, a, b, p, P_x, P_y, q):
        self.p = p
        self.q = q
        self.curve = Curve(a, b, p, P_x, P_y, q)
        self.P = Point(self.curve, P_x, P_y)


def KeyGen(params):
    # Write a function that takes as input an ECDSA_Params object and outputs the key pair (x, Q)
    x = random.randint(1, params.q - 1) # x <- [1, q-1] random
    Q = params.P.scalar_multiply(x) # Q = [x]P
    return (x, Q) # return x, Q

def Sign_FixedNonce(params, k, x, msg):
    # Write a function that takes as input an ECDSA_Params object, a fixed nonce k, a signing key x, and a message msg, and outputs a signature (r, s)
    b_msg = hash_message_to_bits(msg) 
    h_ = bits_to_int(b_msg, params.q) # h = H(msg)
    P_ = params.P.scalar_multiply(k) # P' = [k]P
    r = P_.x % params.q # r = x_coord[P] mod q
    s = mod_inv(k, params.q) * (h_ + (x * r)) % params.q # s = k^-1 * (h + x * r) mod q
    return (r, s)

def Sign(params, x, msg):
    # Write a function that takes as input an ECDSA_Params object, a signing key x, and a message msg, and outputs a signature (r, s)
    # The nonce is to be generated uniformly at random in the appropriate range
    r, s = 0, 0
    while r == 0 or s == 0: # if r != 0 and s! = 0 then return 
        k = random.randint(1, params.q - 1)
        (r, s) = Sign_FixedNonce(params, k, x, msg)
    return (r, s)

def Verify(params, Q, msg, r, s):
    # Write a function that takes as input an ECDSA_Params object, a verification key Q, a message msg, and a signature (r, s)
    # The output should be either 0 (indicating failure) or 1 (indicating success)
    if r > params.q - 1 or r < 1 or s > params.q - 1 or s < 1: # 1<=r<=q-1, 1<=s<=q-1
        return 0
    b_msg = hash_message_to_bits(msg)
    h_ = bits_to_int(b_msg, params.q)
    w = mod_inv(s, params.q) % params.q # w = s^-1 mod q 
    u_1, u_2 = w * h_ % params.q, w * r % params.q # u1 = wh mod q, u2 = wr mod q
    Z = params.P.scalar_multiply(u_1).add(Q.scalar_multiply(u_2)) # Z = [u1]p + [u2]q
    return 1 if r == Z.x % params.q else 0   

from module_1_ECC_ECDSA_tests import run_tests
run_tests(ECDSA_Params, Point, KeyGen, Sign, Sign_FixedNonce, Verify)
