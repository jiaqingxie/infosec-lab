package main

import (
	"time"

	"ethz.ch/netsec/isl/handout/defense/lib"
	"github.com/scionproto/scion/go/lib/slayers"
)

const (
	// Global constants
	Addrlimit = 2
	ASLimit   = 22
	PathLimit = 4
)

var (
	// Here, you can define variables that keep state for your firewall
	num_addr    map[string]int = make(map[string]int, 0)
	num_as      map[string]int = make(map[string]int, 0)
	num_path                   = make(map[string]int)
	lcycle_time time.Time
)

// This function receives all packets destined to the customer server.
//
// Your task is to decide whether to forward or drop a packet based on the
// headers and payload.
// References for the given packet types:
// - SCION header
//   https://pkg.go.dev/github.com/scionproto/scion/go/lib/slayers#SCION
// - UDP header
//   https://pkg.go.dev/github.com/scionproto/scion/go/lib/slayers#UDP
//

func filter(scion slayers.SCION, udp slayers.UDP, payload []byte) bool {
	SrcAddr := string(scion.RawSrcAddr) // source address
	SrcAS := string(scion.SrcIA.A)      // source isd-as
	time0 := time.Since(lcycle_time)    // time spent since last cycle
	len := make([]byte, scion.Path.Len())
	scion.Path.SerializeTo(len)
	path := string(len) // path length

	if time0 > 750*time.Millisecond { // CycleTime, refresh addr & as info
		num_addr = make(map[string]int)
		num_as = make(map[string]int)
		lcycle_time = time.Now()
		num_path = make(map[string]int)
	}

	if num_addr[SrcAddr] >= 1 {
		if num_addr[SrcAddr] < Addrlimit && num_as[SrcAS] < ASLimit {
			num_addr[SrcAddr]++
			num_as[SrcAS]++
			return true
		}
		return false
	} else {
		if num_path[path] >= PathLimit {
			return false
		}
		if num_addr[SrcAddr] < Addrlimit && num_as[SrcAS] < ASLimit {
			num_addr[SrcAddr]++
			num_as[SrcAS]++
			num_path[path]++
			return true
		}
		return false

	}

}

func init() {
	// Perform any initial setup here
}

func main() {
	// lib.parse()
	// Start the firewall. Code after this line will not be executed
	lib.RunFirewall(filter)
}
