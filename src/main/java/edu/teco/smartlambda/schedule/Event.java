package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.runtime.ExecutionResult;
import lombok.Getter;
import lombok.Setter;
import org.quartz.CronExpression;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name = "ScheduleEvent")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	private int id;
	
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	private Key      key;
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambda")
	private Lambda   lambda;
	
	/**
	 * Executes the lambda
	 *
	 * @return future of {@link ExecutionResult}
	 */
	public ListenableFuture<ExecutionResult> execute() {
		return this.getLambda().executeAsync(this.getParameters());
	}
	
	/**
	 * Saves and updates the event in the database
	 */
	public void save() {
		this.setNextExecutionTime();
		this.setLock(null);
		Application.getInstance().getSessionFactory().getCurrentSession().saveOrUpdate(this);
	}
	
	/**
	 * Deletes the event in the database
	 */
	public void delete() {
		Application.getInstance().getSessionFactory().getCurrentSession().delete(this);
	}
	
	private void setNextExecutionTime() {
		try {
			if (this.nextExecution == null) this.nextExecution = Calendar.getInstance();
			
			this.nextExecution.setTime(new CronExpression(this.cronExpression).getNextValidTimeAfter(new Date()));
		} catch (final ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
