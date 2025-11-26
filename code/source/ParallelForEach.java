

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ServiceThread;
import com.wm.lang.ns.NSName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
// --- <<IS-END-IMPORTS>> ---

public final class ParallelForEach

{
	// ---( internal utility methods )---

	final static ParallelForEach _instance = new ParallelForEach();

	static ParallelForEach _newInstance() { return new ParallelForEach(); }

	static ParallelForEach _cast(Object o) { return (ParallelForEach)o; }

	// ---( server methods )---




	public static final void processItems (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(processItems)>> ---
		// @sigtype java 3.5
		// [i] record:1:required items
		// [i] field:0:required svcName
		// [i] field:0:optional numThreads
		// [o] recref:1:required itemStatus ParallelForEach:itemStatus
		IDataCursor pc = pipeline.getCursor();
		NSName svcName = NSName.create(IDataUtil.getString(pc, "svcName"));
		IData[] items = IDataUtil.getIDataArray(pc, "items");
		int numThreads = IDataUtil.getInt(pc, "numThreads", 10);
		List<IData> results = new ArrayList<>(numThreads);
		List<ServiceThread> svcThreads = new ArrayList<>(items.length);
		
		try {
			for (IData item : items){
				IData execPipe = IDataFactory.create();
				IDataCursor epc = execPipe.getCursor();
				IDataUtil.put(epc, "fashionItem", item);
				svcThreads.add(Service.doThreadInvoke(svcName, execPipe));
			}
			for (ServiceThread st : svcThreads){
				IData out = st.getIData();
				results.add(out);
			}
			IDataUtil.put(pc, "itemStatus", results.toArray());
		} catch (Exception e){
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	static class ItemProcessor implements Callable {
		private NSName svcName;
	    private IData pipeline;
	    public ItemProcessor(NSName svcName2, IData pipeline) {
	        this.svcName = svcName2;
	        this.pipeline = pipeline;
	    }
	    @Override
	    public IData call() throws Exception {
	        try {
	            Service.doInvoke(svcName, pipeline);
	            return pipeline;
	        } catch (ServiceException e) {
	            throw new Exception(e);
	        }
	    }
	}
	// --- <<IS-END-SHARED>> ---
}

