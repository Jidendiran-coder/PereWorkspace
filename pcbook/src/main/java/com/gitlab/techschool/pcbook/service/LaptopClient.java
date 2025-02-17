package com.gitlab.techschool.pcbook.service;

import com.gitlab.techschool.pcbook.sample.Generator;
import com.google.protobuf.ByteString;
import com.pcbook.pb.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.optional.qual.Present;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaptopClient {
    private static final Logger logger = Logger.getLogger(LaptopClient.class.getName());

    private final ManagedChannel channel;
    private final LaptopServiceGrpc.LaptopServiceBlockingStub blockingStub;
    private final LaptopServiceGrpc.LaptopServiceStub asyncStub;

    private LaptopClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = LaptopServiceGrpc.newBlockingStub(channel);
        asyncStub = LaptopServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void createLaptop (Laptop laptop) {
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();
        CreateLaptopResponse response = CreateLaptopResponse.getDefaultInstance();

        try{
            response = blockingStub.createLaptop(request);
//            response = blockingStub.withDeadlineAfter(5, TimeUnit.SECONDS).createLaptop(request);
        } catch (StatusRuntimeException e){
            if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                logger.info("laptop ID already exists");
                return;
            }
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        }

        logger.info("Laptop created with ID: " + response.getId());
    }

    private void searchLaptop(Filter filter) {
        logger.info("search started");

        SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
//        Iterator<SearchLaptopResponse> responseIterator = blockingStub.searchLaptop(request);
        try {
            Iterator<SearchLaptopResponse> responseIterator = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .searchLaptop(request);

            while (responseIterator.hasNext()) {
                SearchLaptopResponse response = responseIterator.next();
                Laptop laptop = response.getLaptop();
                logger.info("- found: " + laptop.getId());
            }
        }  catch (Exception e){
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        }

        logger.info("search completed");
    }

    public void uploadImage (String laptopID, String imagePath) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<UploadImageRequest>  requestObserver= asyncStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                .uploadImage(new StreamObserver<UploadImageResponse>() {
                    @Override
                    public void onNext(UploadImageResponse uploadImageResponse) {
                        logger.info("received response: \n" + uploadImageResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.log(Level.SEVERE, "upload failed: " + throwable);
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("image uploaded");
                        finishLatch.countDown();;
                    }
                });

        FileInputStream fileInputStream;
        try {
           fileInputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "cannot read image file : " + e.getMessage());
            return;
        }

        String imageType = imagePath.substring(imagePath.lastIndexOf("."));
        ImageInfo info = ImageInfo.newBuilder().setLaptopId(laptopID).setImageType(imageType).build();
        UploadImageRequest request = UploadImageRequest.newBuilder().setInfo(info).build();

        try {
            requestObserver.onNext(request);
            logger.info("sent image info: \n" + info);

            byte[] buffer = new byte[1024];
            while (true) {
                int n = fileInputStream.read(buffer);
                if (n <= 0) {
                    break;
                }
                if (finishLatch.getCount() == 0) {
                    return;
                }

                request = UploadImageRequest.newBuilder()
                        .setChunkData(ByteString.copyFrom(buffer, 0, n))
                        .build();

                requestObserver.onNext(request);
                logger.info("sent image chunk with size: " + n);
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, "unexpected error: " + e.getMessage());
            requestObserver.onError(e);
            return;
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)){
            logger.warning("request cannot finish within 1 minute");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LaptopClient client = new LaptopClient("0.0.0.0", 8080);

        Generator generator = new Generator();
        //Test create laptop
//        Laptop laptop = generator.NewLaptop().toBuilder().setId("").build()
//        Laptop laptop = generator.NewLaptop().toBuilder().setId("invalid").build();
        // check already exists condition

        try {
//            Test create and search laptops
//            for (int i= 0; i < 10 ; i++){
//                Laptop laptop = generator.NewLaptop();
//                client.createLaptop(laptop);
//            }
//
//            Memory minRam = Memory.newBuilder()
//                    .setValue(8)
//                    .setUnit(Memory.Unit.GIGABYTE)
//                    .build();
//
//            Filter filter = Filter.newBuilder()
//                    .setMaxPriceUsd(3000)
//                    .setMinCpuCores(4)
//                    .setMinCpuGhz(2.5)
//                    .setMinRam(minRam)
//                    .build();
//
//            client.searchLaptop(filter);

//            Test upload laptop image
            Laptop laptop = generator.NewLaptop();
//            client.createLaptop(laptop);
            client.uploadImage(laptop.getId(), "tmp/laptop.jpg");
        } finally {
            client.shutdown();
        }
    }
}
