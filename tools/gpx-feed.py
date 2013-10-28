#!/usr/bin/env python3
#
# gpx-feed.py - simple program to feed GPX file coordinates to Android
# emulator.
#
# For usage, try --help.
#
import gpxpy
import argparse
from datetime import datetime
from time import sleep
from sys import exit
from signal import signal, SIGPIPE, SIG_DFL
from socket import socket, AF_INET, SOCK_STREAM

def host_port(s):
    if ':' in s:
        (host, port) = s.split(":")
    else:
        host = s
        port = 5554

    if not host:
        host = "localhost"

    return (host, int(port))

def main():
    parser = argparse.ArgumentParser(description='Feed GPX coordinates to Android emulator. This is useful to feed in a GPS track acquired from elsewhere for the purpose of testing location handling code in an android application.')

    parser.add_argument('--fixed-interval', '-f',
                        type=float, default=None,
                        help='Fixed interval between sending coordinates (default is none, e.g. follows GPX file timestamps with --default-interval and --speed -- but --fixed-interval ignores --speed)')

    parser.add_argument('--default-interval', '-i',
                        type=int, default=15,
                        help='Time between track points if none is present in the track')

    parser.add_argument('--speed', '-s', type=float, default=1.0,
                        help='Speed of following time intervals (-s10 is ten times as fast, -s0.2 is one-fifth speed e.g. five times slower)')

    parser.add_argument('--max-interval', '-m', type=int, default=15,
                        help='Maximum interval between sending coordinates')

    parser.add_argument('--once', '-o', action='store_true', help='Go through GPX file only once (default is to repeat)', default=False)
    parser.add_argument('file', metavar='GPX-FILE', nargs=1, type=str,

                        help='GPX file name')

    parser.add_argument('host_port',
                        metavar='HOST:PORT|HOST|:PORT', nargs='?',
                        type=host_port,
                        default=host_port("localhost:5554"),
                        help='Android emulator port (default localhost:5554)')

    args = parser.parse_args()

    gpx = gpxpy.parse(open(args.file[0], "r"))
    assert(len(gpx.tracks) > 0)

    s = socket(AF_INET, SOCK_STREAM)
    s.connect(args.host_port)
    s = s.makefile("rw")
    res = s.readline().rstrip()
    print("Remote emulator says: {0!r}".format(res))

    res = s.readline().rstrip()
    if res != "OK":
        print("Expected OK from emulator, got: {0!r}".format(res))

    times = 0

    last = datetime.now()

    while not args.once or times < 1:
        for track in gpx.tracks:
            for segment in track.segments:
                for point in segment.points:
                    if point.time:
                        interval = (point.time - last).total_seconds()
                        last = point.time
                    else:
                        interval = args.default_interval
                        last_time += interval

                    interval = min(max(0, interval), args.max_interval)
                    interval /= args.speed

                    if args.fixed_interval:
                        interval = args.fixed_interval

                    #geo fix <longitude value> <latitude value>
                    msg = "geo fix %f %f\n" % (point.longitude,
                                               point.latitude)
                    s.write(msg)
                    s.flush()

                    res = s.readline().rstrip()
                    if res != "OK":
                        print("Sent {0!r}, got unexpected reply: {1!r}".format(msg, res))

                    print("{0:>12} / {1:<12} {2:>4}s sleep".format(point.latitude,
                                                         point.longitude,
                                                         interval))

                    sleep(interval)

if __name__ == '__main__':
    signal(SIGPIPE, SIG_DFL)
    main()
