package ASyncTest.compression.asynccomp;

import ASyncTest.compression.BaseCompressor;
import ASyncTest.runners.TestMetadata;
import ASyncTest.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * A class for performing the dependency compression algorithm described in the Anti-Freeze paper.
 */
public class AsyncCompressor extends AsyncBaseCompressor {

    private final int COMPRESSION_CONSTANT;

    public AsyncCompressor(final int compressionConstant) {
        this.COMPRESSION_CONSTANT = compressionConstant;
    }

    private AsyncCompressor(TestMetadata metadata, AsyncBaseTest test, final int compressionConstant) {
        super(metadata, test);
        this.COMPRESSION_CONSTANT = compressionConstant;
    }

    @Override
    protected BaseCompressor newCompressor(TestMetadata metadata, AsyncBaseTest testCase) {
        return new AsyncCompressor(metadata, testCase, this.COMPRESSION_CONSTANT);
    }

    @Override
    protected void compress() {
        super.getMetadata().startNumberOfDependents = dependencies.size();
        super.getMetadata().compStartTime = System.currentTimeMillis();
        while (dependencies.size() > this.COMPRESSION_CONSTANT) {
            //System.out.println("dependencies.size() " + dependencies.size());
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            Ref best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    Ref bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    Ref overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area == 0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i = dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }

                    if (best_area == 0) {
                        break;
                    }
                }
                if (best_area == 0) {
                    break;
                }
            }
            // Merge i,j
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
        super.getMetadata().compFinalTime = System.currentTimeMillis();
        super.getMetadata().finalNumberOfDependents = dependencies.size();
    }

}
