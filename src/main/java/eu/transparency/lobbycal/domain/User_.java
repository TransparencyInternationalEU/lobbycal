package eu.transparency.lobbycal.domain;

import java.time.ZonedDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-10T21:42:32.202+0100")
@StaticMetamodel(User.class)
public class User_ extends AbstractAuditingEntity {
	public static volatile SingularAttribute<User, Long> id;
	public static volatile SingularAttribute<User, String> login;
	public static volatile SingularAttribute<User, String> password;
	public static volatile SingularAttribute<User, String> firstName;
	public static volatile SingularAttribute<User, String> lastName;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SingularAttribute<User, Boolean> activated;
	public static volatile SingularAttribute<User, Boolean> notificationEnabled;
	public static volatile SingularAttribute<User, Boolean> notificationOfSubmittersEnabled;
	public static volatile SingularAttribute<User, Boolean> showFutureMeetings;
	public static volatile SingularAttribute<User, Boolean> lobbycloudSharingEnabled;
	public static volatile SingularAttribute<User, String> langKey;
	public static volatile SingularAttribute<User, String> activationKey;
	public static volatile SingularAttribute<User, String> resetKey;
	public static volatile SingularAttribute<User, ZonedDateTime> resetDate;
	public static volatile SingularAttribute<User, ZonedDateTime> lastNotified;
	public static volatile SetAttribute<User, Authority> authorities;
	public static volatile SetAttribute<User, Alias> aliases;
	public static volatile SetAttribute<User, PersistentToken> persistentTokens;
	public static volatile SetAttribute<User, Meeting> meetings;
}
