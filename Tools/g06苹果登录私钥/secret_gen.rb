require "jwt"

key_file = "/Users/hyz/Desktop/AuthKey_X53G9XBWV3.p8"
team_id = "1494248833"
client_id = "com.oceanvista.g06"
key_id = "ZJ3U448NKH"
validity_period = 180 # In days. Max 180 (6 months) according to Apple docs.

private_key = OpenSSL::PKey::EC.new IO.read key_file

token = JWT.encode(
	{
		iss: team_id,
		iat: Time.now.to_i,
		exp: Time.now.to_i + 86400 * validity_period,
		aud: "https://appleid.apple.com",
		sub: client_id
	},
	private_key,
	"ES256",
	header_fields=
	{
		kid: key_id 
	}
)
puts token