# RELEASE NOTES

## Version 0.2.0, 0.2.1 & 0.2.2

If you are upgrading from a previous release, you will need to make several
changes:

#### Update the client config file `/var/www/sshkeyportal/conf/cfg.xml`

* Add the list of required scopes into a `<scopes>` element in the relevant
  `<client>` element(s).  
  By default the basic scopes are enabled
  (`openid`, `email`, `profile` and `edu.uiuc.ncsa.myproxy.getcert`).  
  You should **disable** `edu.uiuc.ncsa.myproxy.getcert` and probably
  want to **add** `org.cilogon.userinfo`.  
  (You can also disable email, unless you want to use it as in the next bullet).  
  If the MasterPortal is configured to require a specific scope, such as
  `eu.rcauth.sshkeys`, make sure to **add** it.

        <scopes>
            <scope enabled="false">edu.uiuc.ncsa.myproxy.getcert</scope>
            <scope enabled="false">email</scope>
            <scope>org.cilogon.userinfo</scope>
            <scope>eu.rcauth.sshkeys</scope>
        </scopes>

* By default, the portal now prints the user's name
  (*name* or *given name+family name*), username and IdP's display name.  
  You can configure which claims are used for each of these by configuring
  one or more of the elements
    * `<nameClaim>` (default claim `name`)
    * `<givenNameClaim>` (default claim `given_name`)
    * `<familyNameClaim>` (default claim `family_name`)
    * `<idpDisplayNameClaim>` (default claim `idp_display_name`)

  for example

        <nameClaim>testclaim</nameClaim>

* Make sure you have configured a `wellKnownURI` element for the Master Portal
  in the relevant `<client>` element(s):
    * `<wellKnownUri>https://mp.example.org/mp-oa2-server/.well-known/openid-configuration</wellKnownUri>`

  When absent, signed tokens will not be verified.

* Add the following element to the relevant `<client>` element(s):
    * `<OIDCEnabled>true</OIDCEnabled>`

  This is currently optional, being the default setting.
