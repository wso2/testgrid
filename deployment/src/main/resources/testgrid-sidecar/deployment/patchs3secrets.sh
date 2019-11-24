S3_KEY_ID_nb64=$1
S3_SECRET_KEY_nb64=$2

S3_KEY_ID=$(echo -n ${S3_KEY_ID_nb64} | base64)
S3_SECRET_KEY=$(echo -n ${S3_SECRET_KEY_nb64} | base64)

sed -e "s|\${S3_KEY_ID}|${S3_KEY_ID}|g" | sed -e "s|\${S3_SECRET_KEY}|${S3_SECRET_KEY}|g"