package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Getter;
import lombok.Setter;
import org.quartz.CronExpression;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Melanie on 01.02.2017.
 */
@Entity
@Table(name = "ScheduleEvent")
public class Event {
	@Getter
	@Setter
	private String   cronExpression;
	@Getter
	@Setter
	private Calendar nextExecution;
	@Getter
	@Setter
	private String   name;
	@Getter
	@Setter
	private String   parameters;
	@Getter
	@Setter
	private Calendar lock;
	@Getter
	@Setter
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	private Key      key;
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambda")
	private Lambda   lambda;
	
	public ListenableFuture<ExecutionReturnValue> execute() {
		return lambda.executeAsync(parameters);
	}
	
	public void save() {
		setNextExecutionTime();
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	private void setNextExecutionTime() {
		try {
			this.nextExecution.setTime(new CronExpression(this.cronExpression).getNextValidTimeAfter(new Date()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
