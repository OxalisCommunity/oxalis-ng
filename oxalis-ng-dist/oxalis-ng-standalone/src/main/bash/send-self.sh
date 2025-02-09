#!/usr/bin/env bash

#
# Sends sample invoice to ourselves
#

PRG="$0"

# Name of directory from which this program is being executed, should normally result in the bin/ directory
# of the distribution.
PRGDIR=`dirname $0`


RECEIVER="9908:983995691"

# Peppol Participant id for Difi
DIFI_TEST="9908:810418052"

#
# Allows overriding from command line
#
for i in "$@"
do
	case $i in
		-r=*)
		RECEIVER="${i#*=}"
		shift
		;;
	esac
done

echo "Sender eksempel faktura (bare tull) fra og til oss selv"

JARFILE=$PRGDIR/oxalis-ng-standalone.jar

if [ ! -r "$JARFILE" ]; then
	echo "$JARFILE not found"
	exit 4
fi

java -jar $JARFILE -f ./sample-invoice.xml -s 9908:976098897 -r ${RECEIVER} -u http://localhost:8080/oxalis-ng/as4 -e data/evidence

# --cert /Users/steinar/.oxalis/difi-cert.pem \
# -u http://localhost:8080/oxalis-ng/as4 -f /var/peppol/samples/hfcEHF_P205044_P3746797_5684_HF_PTI_161107_2115_11897047_216879680363.XML