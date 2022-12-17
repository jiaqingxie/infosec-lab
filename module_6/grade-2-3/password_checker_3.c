
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int check_password(char* p, int p_size,  char* i, int i_size){
    int len = 0;
    int bad = 0;

    for (int pos = 0; pos < p_size; ++pos){
		if(p[pos] != '$'){
			++len;
		}
    }

    for (int pos = 0; pos < i_size; ++pos){
        bad += (p[pos + 15 - i_size] != i[pos]);
    }

    return ((bad == 0) & len == i_size);
}

//assumptions: password only has small characters [a, z], maximum length is 15 characters
int main (int argc, char* argv[]){

	if (argc != 3) {
		fprintf(stderr, "Usage: %s <password guess> <output_file>\n", argv[0]);
		exit(EXIT_FAILURE);
	}

	FILE* password_file;
	password_file = fopen ("/home/isl/t2_3/password.txt", "r");
	if (password_file == NULL) {
		perror("cannot open password file\n");
		exit(EXIT_FAILURE);
	}

	char password [16] = "\0";
    int ch;
    for (int i = 0; i<15 &&(ch=getc(password_file)) != EOF; ++i){
        password[i] = ch;
    }

    int is_match = 0;
    is_match = check_password(password, strlen(password), argv[1], strlen(argv[1]));
       
	FILE* output_file;
	output_file = fopen(argv[2], "wb");
	fputc(is_match, output_file);
	fclose(output_file);

	fclose(password_file);
	return 0;
}
