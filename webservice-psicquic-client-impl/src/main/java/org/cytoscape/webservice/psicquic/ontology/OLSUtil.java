package org.cytoscape.webservice.psicquic.ontology;


public class OLSUtil {

//	// Ontology Name for PSI-MI
//	private static final String PSIMI = "MI";
//
//	private static final String ITR_TYPE = "MI:0190";
//
//	private static final String DB_ROOT = "MI:0444";
//
//	private static Query ols;
//	private static EBISearchService eport;
//
//	static {
//		QueryService locator = new QueryServiceLocator();
//		EBISearchService_Service ebeye = new EBISearchService_Service();
//		eport = ebeye.getEBISearchServiceHttpPort();
//		try {
//			ols = locator.getOntologyQuery();
//		} catch (ServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public static List<String> keywordSearch(String key) {
//		int count = eport.getNumberOfResults("uniprot", key);
//
//		List<String> firstResult = eport.getResultsIds("uniprot", key, 0, 100)
//				.getString();
//		if (count <= 100) {
//			return firstResult;
//		} else {
//			List<String> result = new ArrayList<String>(firstResult);
//			int index = 100;
//			while (index < count) {
//				result.addAll(eport.getResultsIds("uniprot", key, index, 100)
//						.getString());
//				index = index + 100;
//			}
//			return result;
//		}
//	}
//
//	public static void getOntologyList() throws IOException {
//
//		Map terms = ols.getOntologyNames();
//
//		for (Object key : terms.keySet()) {
//			System.out.println("(key, val) = " + key + ", " + terms.get(key));
//		}
//
//		ArrayOfString domain = eport.listDomains();
//
//		List<String> list = domain.getString();
//		for (String d : list) {
//			System.out.println("domain = " + d);
//		}
//
//		System.out.println("list size = " + terms.keySet().size());
//		System.out.println("V = " + ols.getVersion());
//
//		List<String> searchRes = keywordSearch("p53 human");
//
//		for (String d : searchRes) {
//			System.out.println(d);
//		}
//	}
//
//	public static Map<String, String> getSynonymMap(List<String> idList) {
//
//		return null;
//	}
//
//	public static Map<String, String> getAllChildren(String term)
//			throws IOException {
//		return ols.getTermChildren(term, PSIMI, -1, null);
//
//		//		
//		// System.out.println("========================================");
//		// Map<String, String> rootTerms = ols.getTermChildren(ITR_TYPE, PSIMI,
//		// 1, null);
//		// for(String key: rootTerms.keySet()) {
//		// System.out.println("(key, root val) = " + key + ", " +
//		// rootTerms.get(key));
//		// }
//
//	}
//
//	public static String toCanonicalName() throws IOException {
//		Map<String, String> dbTerms = ols.getTermChildren(DB_ROOT, PSIMI, -1,
//				null);
//
//		return null;
//	}

}
